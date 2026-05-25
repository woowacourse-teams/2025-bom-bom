package me.bombom.api.v1.subscribe.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.common.DiscordWebhookNotifier;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.RetryableException;
import me.bombom.api.v1.common.exception.UnauthorizedException;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterDetailRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.api.v1.subscribe.domain.NewsletterSubscriptionCount;
import me.bombom.api.v1.subscribe.domain.Subscribe;
import me.bombom.api.v1.subscribe.domain.SubscribeStatus;
import me.bombom.api.v1.subscribe.dto.response.SubscribedNewsletterResponse;
import me.bombom.api.v1.subscribe.exception.AutoUnsubscribeFailedException;
import me.bombom.api.v1.subscribe.repository.NewsletterSubscriptionCountRepository;
import me.bombom.api.v1.subscribe.repository.SubscribeRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@IntegrationTest
class SubscribeServiceTest {

    @Autowired
    private SubscribeService subscribeService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NewsletterRepository newsletterRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Autowired
    private NewsletterDetailRepository newsletterDetailRepository;

    @Autowired
    private NewsletterSubscriptionCountRepository newsletterSubscriptionCountRepository;

    @MockitoBean
    private UnsubscribeAgent unsubscribeAgent;

    @MockitoBean
    private DiscordWebhookNotifier discordNotifier;

    @MockitoBean
    private UnsubscribeRetryService unsubscribeRetryService;

    @AfterEach
    void tearDown() {
        subscribeRepository.deleteAllInBatch();
        newsletterSubscriptionCountRepository.deleteAllInBatch();
        newsletterRepository.deleteAllInBatch();
        newsletterDetailRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    void 구독중인_뉴스레터를_조회한다() {
        // given
        Member member = memberRepository.save(TestFixture.uniqueMemberFixture());

        List<Category> categories = TestFixture.createCategories();
        categoryRepository.saveAll(categories);

        List<NewsletterDetail> newsletterDetails = TestFixture.createNewsletterDetails();
        newsletterDetailRepository.saveAll(newsletterDetails);
        List<Newsletter> newsletters = TestFixture.createNewslettersWithDetails(categories, newsletterDetails);
        newsletterRepository.saveAll(newsletters);

        subscribeRepository.saveAll(List.of(
                Subscribe.builder().memberId(member.getId()).newsletterId(newsletters.getFirst().getId()).build(),
                Subscribe.builder().memberId(member.getId()).newsletterId(newsletters.getLast().getId()).build()
        ));

        // when
        List<SubscribedNewsletterResponse> result = subscribeService.getSubscribedNewsletters(member);

        // then
        assertThat(result).hasSize(2)
                .extracting("name")
                .containsExactlyInAnyOrder(newsletters.getFirst().getName(), newsletters.getLast().getName());
    }

    @Test
    void 구독을_취소한다() {
        // given
        Member member = memberRepository.save(TestFixture.uniqueMemberFixture());

        Category category = categoryRepository.save(TestFixture.createCategory());
        NewsletterDetail newsletterDetail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(true));
        Newsletter newsletter = newsletterRepository.save(
                TestFixture.createNewsletter("테스트 뉴스레터", "test@test.com", category.getId(), newsletterDetail.getId()));
        Subscribe subscribe = subscribeRepository.save(Subscribe.builder()
                .memberId(member.getId())
                .newsletterId(newsletter.getId())
                .build());

        // when
        subscribeService.unsubscribe(member.getId(), subscribe.getId());

        // then
        Subscribe result = subscribeRepository.findById(subscribe.getId()).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(me.bombom.api.v1.subscribe.domain.SubscribeStatus.UNSUBSCRIBING);
    }

    @Test
    void 다른_사람의_구독을_취소하면_예외가_발생한다() {
        // given
        Member member = memberRepository.save(TestFixture.uniqueMemberFixture());

        Member otherMember = TestFixture.createUniqueMember("other", "otherProvider");
        memberRepository.save(otherMember);

        Category category = categoryRepository.save(TestFixture.createCategory());
        NewsletterDetail newsletterDetail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(true));
        Newsletter newsletter = newsletterRepository.save(
                TestFixture.createNewsletter("테스트 뉴스레터", "test@test.com", category.getId(), newsletterDetail.getId()));
        Subscribe subscribe = subscribeRepository.save(Subscribe.builder()
                .memberId(otherMember.getId())
                .newsletterId(newsletter.getId())
                .build());

        // when & then
        assertThatThrownBy(() -> subscribeService.unsubscribe(member.getId(), subscribe.getId()))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void 존재하지_않는_구독을_취소하면_예외가_발생한다() {
        // given
        Member member = memberRepository.save(TestFixture.uniqueMemberFixture());

        // when & then
        assertThatThrownBy(() -> subscribeService.unsubscribe(member.getId(), 999L))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void unsubscribeUrl이_있는_구독을_취소하면_unsubscribeUrl을_반환한다() {
        // given
        Member member = memberRepository.save(TestFixture.uniqueMemberFixture());

        Category category = categoryRepository.save(TestFixture.createCategory());
        NewsletterDetail newsletterDetail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(true));
        Newsletter newsletter = newsletterRepository.save(
                TestFixture.createNewsletter(
                        "테스트 뉴스레터",
                        "test@test.com",
                        category.getId(),
                        newsletterDetail.getId()
                )
        );
        String unsubscribeUrl = "https://example.com/unsubscribe";
        Subscribe subscribe = subscribeRepository.save(Subscribe.builder()
                .memberId(member.getId())
                .newsletterId(newsletter.getId())
                .unsubscribeUrl(unsubscribeUrl)
                .build()
        );

        // when
        subscribeService.unsubscribe(member.getId(), subscribe.getId());

        // then
        Subscribe result = subscribeRepository.findById(subscribe.getId()).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(SubscribeStatus.UNSUBSCRIBING);
    }

    @Test
    void FAILED_상태의_구독을_취소하면_강제_삭제된다() {
        // given
        Member member = memberRepository.save(createMemberWithBirthDate(LocalDate.of(2001, 1, 1)));

        Category category = categoryRepository.save(TestFixture.createCategory());
        NewsletterDetail newsletterDetail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(true));
        Newsletter newsletter = newsletterRepository.save(
                TestFixture.createNewsletter(
                        "테스트 뉴스레터",
                        "test@test.com",
                        category.getId(),
                        newsletterDetail.getId()
                )
        );

        Subscribe subscribe = subscribeRepository.save(Subscribe.builder()
                .memberId(member.getId())
                .newsletterId(newsletter.getId())
                .build()
        );
        newsletterSubscriptionCountRepository.save(
                NewsletterSubscriptionCount.builder()
                        .newsletterId(newsletter.getId())
                        .total(1)
                        .age20s(1)
                        .build()
        );

        // FAILED 상태로 변경
        subscribe.changeStatus(me.bombom.api.v1.subscribe.domain.SubscribeStatus.UNSUBSCRIBE_FAILED);
        subscribeRepository.save(subscribe);

        // when
        subscribeService.unsubscribe(member.getId(), subscribe.getId());

        // then
        assertThat(subscribeRepository.findById(subscribe.getId())).isEmpty();
        NewsletterSubscriptionCount count = newsletterSubscriptionCountRepository.findAll().getFirst();
        assertThat(count.getTotal()).isZero();
        assertThat(count.getAge20s()).isZero();
    }

    @Test
    void UNSUBSCRIBING_상태의_구독을_취소하면_중복_방지로_응답만_반환한다() {
        // given
        Member member = memberRepository.save(TestFixture.uniqueMemberFixture());

        Category category = categoryRepository.save(TestFixture.createCategory());
        NewsletterDetail newsletterDetail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(true));
        Newsletter newsletter = newsletterRepository.save(
                TestFixture.createNewsletter(
                        "테스트 뉴스레터",
                        "test@test.com",
                        category.getId(),
                        newsletterDetail.getId())
        );

        String unsubscribeUrl = "https://example.com/unsubscribe";
        Subscribe subscribe = subscribeRepository.save(Subscribe.builder()
                .memberId(member.getId())
                .newsletterId(newsletter.getId())
                .unsubscribeUrl(unsubscribeUrl)
                .build()
        );

        // UNSUBSCRIBING 상태로 변경
        subscribe.changeStatus(SubscribeStatus.UNSUBSCRIBING);
        subscribeRepository.save(subscribe);

        // when
        subscribeService.unsubscribe(member.getId(), subscribe.getId());

        // then
        assertThat(subscribeRepository.findById(subscribe.getId())).isPresent();
    }

    @Test
    void SUBSCRIBED_상태의_구독을_취소하면_UNSUBSCRIBING_상태로_변경된다() {
        // given
        Member member = memberRepository.save(TestFixture.uniqueMemberFixture());

        Category category = categoryRepository.save(TestFixture.createCategory());
        NewsletterDetail newsletterDetail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(true));
        Newsletter newsletter = newsletterRepository.save(
                TestFixture.createNewsletter(
                        "테스트 뉴스레터",
                        "test@test.com",
                        category.getId(),
                        newsletterDetail.getId())
        );

        Subscribe subscribe = subscribeRepository.save(Subscribe.builder()
                .memberId(member.getId())
                .newsletterId(newsletter.getId())
                .build()
        );

        // when
        subscribeService.unsubscribe(member.getId(), subscribe.getId());

        // then
        Subscribe result = subscribeRepository.findById(subscribe.getId()).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(SubscribeStatus.UNSUBSCRIBING);
    }

    @Test
    void handleUnsubscribeResult_성공시_구독을_삭제한다() {
        // given
        Member member = memberRepository.save(createMemberWithBirthDate(LocalDate.of(2001, 1, 1)));

        Category category = categoryRepository.save(TestFixture.createCategory());
        NewsletterDetail newsletterDetail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(true));
        Newsletter newsletter = newsletterRepository.save(
                TestFixture.createNewsletter(
                        "테스트 뉴스레터",
                        "test@test.com",
                        category.getId(),
                        newsletterDetail.getId()
                )
        );

        Subscribe subscribe = subscribeRepository.save(Subscribe.builder()
                .memberId(member.getId())
                .newsletterId(newsletter.getId())
                .build()
        );
        newsletterSubscriptionCountRepository.save(
                NewsletterSubscriptionCount.builder()
                        .newsletterId(newsletter.getId())
                        .total(1)
                        .age20s(1)
                        .build()
        );

        // when
        subscribeService.handleUnsubscribeResult(subscribe.getId(), true);

        // then
        assertThat(subscribeRepository.findById(subscribe.getId())).isEmpty();
        NewsletterSubscriptionCount count = newsletterSubscriptionCountRepository.findAll().getFirst();
        assertThat(count.getTotal()).isZero();
        assertThat(count.getAge20s()).isZero();
    }

    @Test
    void handleUnsubscribeResult_실패시_FAILED_상태로_변경한다() {
        // given
        Member member = memberRepository.save(TestFixture.uniqueMemberFixture());

        Category category = categoryRepository.save(TestFixture.createCategory());
        NewsletterDetail newsletterDetail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(true));
        Newsletter newsletter = newsletterRepository.save(
                TestFixture.createNewsletter(
                        "테스트 뉴스레터",
                        "test@test.com",
                        category.getId(),
                        newsletterDetail.getId())
        );

        Subscribe subscribe = subscribeRepository.save(Subscribe.builder()
                .memberId(member.getId())
                .newsletterId(newsletter.getId())
                .build()
        );

        // when
        subscribeService.handleUnsubscribeResult(subscribe.getId(), false);

        // then
        Subscribe result = subscribeRepository.findById(subscribe.getId()).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(me.bombom.api.v1.subscribe.domain.SubscribeStatus.UNSUBSCRIBE_FAILED);
    }

    @Test
    void 구독_해지_성공_시_람다를_호출하고_완료_처리한다() {
        // given
        Member member = memberRepository.save(TestFixture.uniqueMemberFixture());
        Category category = categoryRepository.save(TestFixture.createCategory());
        NewsletterDetail newsletterDetail = newsletterDetailRepository
                .save(TestFixture.createNewsletterDetail(true));
        Newsletter newsletter = newsletterRepository.save(
                TestFixture.createNewsletter("테스트 뉴스레터", "test@test.com", category.getId(),
                        newsletterDetail.getId()));
        Subscribe s = subscribeRepository.save(Subscribe.builder()
                .memberId(member.getId())
                .newsletterId(newsletter.getId())
                .build());

        Long subscribeId = s.getId();
        Long newsletterId = newsletter.getId();
        String unsubscribeUrl = "https://example.com/unsub";

        // when
        subscribeService.processUnsubscribe(subscribeId, newsletterId, unsubscribeUrl);

        // then
        verify(unsubscribeAgent, times(1)).unsubscribe(unsubscribeUrl, newsletterId);
        verify(unsubscribeRetryService, times(1)).deleteIfExists(subscribeId);
        assertThat(subscribeRepository.findById(subscribeId)).isEmpty();
    }

    @Test
    void 재시도_가능_에러_발생_시_재시도를_스케줄링한다() {
        // given
        Member member = memberRepository.save(TestFixture.uniqueMemberFixture());
        Category category = categoryRepository.save(TestFixture.createCategory());
        NewsletterDetail newsletterDetail = newsletterDetailRepository
                .save(TestFixture.createNewsletterDetail(true));
        Newsletter newsletter = newsletterRepository.save(
                TestFixture.createNewsletter("테스트 뉴스레터", "test@test.com", category.getId(),
                        newsletterDetail.getId()));
        Subscribe s = subscribeRepository.save(Subscribe.builder()
                .memberId(member.getId())
                .newsletterId(newsletter.getId())
                .build());

        Long subscribeId = s.getId();
        Long newsletterId = newsletter.getId();
        String unsubscribeUrl = "https://example.com/unsub";
        doThrow(new RetryableException("Server Error"))
                .when(unsubscribeAgent).unsubscribe(anyString(), anyLong());
        given(unsubscribeRetryService.scheduleRetry(anyLong(), anyString())).willReturn(true);

        // when
        subscribeService.processUnsubscribe(subscribeId, newsletterId, unsubscribeUrl);

        // then
        verify(unsubscribeRetryService, times(1)).scheduleRetry(eq(subscribeId), eq("Server Error"));
    }

    @Test
    void 영구_실패_에러_발생_시_알림을_보낸다() {
        // given
        Member member = memberRepository.save(TestFixture.uniqueMemberFixture());
        Category category = categoryRepository.save(TestFixture.createCategory());
        NewsletterDetail newsletterDetail = newsletterDetailRepository
                .save(TestFixture.createNewsletterDetail(true));
        Newsletter newsletter = newsletterRepository.save(
                TestFixture.createNewsletter("테스트 뉴스레터", "test@test.com", category.getId(),
                        newsletterDetail.getId()));
        Subscribe s = subscribeRepository.save(Subscribe.builder()
                .memberId(member.getId())
                .newsletterId(newsletter.getId())
                .build());

        Long subscribeId = s.getId();
        Long newsletterId = newsletter.getId();
        String unsubscribeUrl = "https://example.com/unsub";
        doThrow(new AutoUnsubscribeFailedException("Invalid URL", newsletterId, unsubscribeUrl))
                .when(unsubscribeAgent).unsubscribe(anyString(), anyLong());

        // when
        subscribeService.processUnsubscribe(subscribeId, newsletterId, unsubscribeUrl);

        // then
        verify(discordNotifier, times(1))
                .sendUnsubscribeErrorNotification(
                        eq("Invalid URL"),
                        argThat(sub -> sub.getId().equals(s.getId())),
                        eq(unsubscribeUrl)
                );

        Subscribe result = subscribeRepository.findById(subscribeId).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(SubscribeStatus.UNSUBSCRIBE_FAILED);
    }

    private Member createMemberWithBirthDate(LocalDate birthDate) {
        Member member = TestFixture.uniqueMemberFixture();
        return Member.builder()
                .provider(member.getProvider())
                .providerId(member.getProviderId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileImageUrl())
                .birthDate(birthDate)
                .gender(member.getGender())
                .roleId(member.getRoleId())
                .build();
    }
}
