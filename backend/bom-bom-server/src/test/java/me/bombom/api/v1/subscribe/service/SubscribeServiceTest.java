package me.bombom.api.v1.subscribe.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.TestFixture;
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
import me.bombom.api.v1.subscribe.domain.UnsubscribeRetry;
import me.bombom.api.v1.subscribe.dto.response.SubscribedNewsletterResponse;
import me.bombom.api.v1.subscribe.exception.AutoUnsubscribeFailedException;
import me.bombom.api.v1.subscribe.repository.NewsletterSubscriptionCountRepository;
import me.bombom.api.v1.subscribe.repository.SubscribeRepository;
import me.bombom.api.v1.subscribe.repository.UnsubscribeRetryRepository;
import me.bombom.support.notification.FakeDiscordWebhookNotifier;
import me.bombom.support.subscribe.FakeUnsubscribeAgent;
import me.bombom.support.integration.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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

    @Autowired
    private FakeUnsubscribeAgent unsubscribeAgent;

    @Autowired
    private FakeDiscordWebhookNotifier discordNotifier;

    @Autowired
    private UnsubscribeRetryRepository unsubscribeRetryRepository;

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
    void 회원과_뉴스레터로_구독을_삭제하면_구독자_수가_감소한다() {
        Member member = memberRepository.save(createMemberWithBirthDate(LocalDate.of(2001, 1, 1)));

        Category category = categoryRepository.save(TestFixture.createCategory());
        NewsletterDetail newsletterDetail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(true));
        Newsletter newsletter = newsletterRepository.save(
                TestFixture.createNewsletter("테스트 뉴스레터", "test@test.com", category.getId(), newsletterDetail.getId()));
        subscribeRepository.save(Subscribe.builder()
                .memberId(member.getId())
                .newsletterId(newsletter.getId())
                .build());
        newsletterSubscriptionCountRepository.save(
                NewsletterSubscriptionCount.builder()
                        .newsletterId(newsletter.getId())
                        .total(1)
                        .age20s(1)
                        .build()
        );

        subscribeService.deleteByMemberIdAndNewsletterId(member.getId(), newsletter.getId());

        assertThat(subscribeRepository.findAll()).isEmpty();
        NewsletterSubscriptionCount count = newsletterSubscriptionCountRepository.findAll().getFirst();
        assertThat(count.getTotal()).isZero();
        assertThat(count.getAge20s()).isZero();
    }

    @Test
    void 회원의_모든_구독을_삭제하면_각_구독자_수가_감소한다() {
        Member member = memberRepository.save(createMemberWithBirthDate(LocalDate.of(2001, 1, 1)));

        Category category = categoryRepository.save(TestFixture.createCategory());
        NewsletterDetail detail1 = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(true));
        NewsletterDetail detail2 = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(false));
        Newsletter newsletter1 = newsletterRepository.save(
                TestFixture.createNewsletter("테스트 뉴스레터1", "test1@test.com", category.getId(), detail1.getId()));
        Newsletter newsletter2 = newsletterRepository.save(
                TestFixture.createNewsletter("테스트 뉴스레터2", "test2@test.com", category.getId(), detail2.getId()));

        subscribeRepository.saveAll(List.of(
                Subscribe.builder().memberId(member.getId()).newsletterId(newsletter1.getId()).build(),
                Subscribe.builder().memberId(member.getId()).newsletterId(newsletter2.getId()).build()
        ));
        newsletterSubscriptionCountRepository.saveAll(List.of(
                NewsletterSubscriptionCount.builder().newsletterId(newsletter1.getId()).total(1).age20s(1).build(),
                NewsletterSubscriptionCount.builder().newsletterId(newsletter2.getId()).total(1).age20s(1).build()
        ));

        subscribeService.deleteAllByMemberId(member.getId());

        assertThat(subscribeRepository.findAll()).isEmpty();
        assertThat(newsletterSubscriptionCountRepository.findAll())
                .allSatisfy(count -> {
                    assertThat(count.getTotal()).isZero();
                    assertThat(count.getAge20s()).isZero();
                });
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
    void 해지_결과_대상_구독이_없으면_무시한다() {
        subscribeService.handleUnsubscribeResult(-1L, true);

        assertThat(subscribeRepository.findAll()).isEmpty();
    }

    @Test
    void 재시도할_구독이_없으면_재시도_항목을_삭제한다() {
        // given
        UnsubscribeRetry retry = unsubscribeRetryRepository.save(UnsubscribeRetry.builder()
                .subscribeId(-1L)
                .nextRetryAt(LocalDateTime.now())
                .lastError("not found")
                .build());

        // when
        subscribeService.retryUnsubscribe(retry.getSubscribeId());

        // then
        assertThat(unsubscribeRetryRepository.findById(retry.getId())).isEmpty();
        assertThat(unsubscribeAgent.getRequests()).isEmpty();
    }

    @Test
    void 재시도할_구독이_있으면_해지_처리하고_재시도_항목을_삭제한다() {
        // given
        Member member = memberRepository.save(TestFixture.uniqueMemberFixture());
        Subscribe subscribe = saveSubscribe(member);
        UnsubscribeRetry retry = unsubscribeRetryRepository.save(UnsubscribeRetry.builder()
                .subscribeId(subscribe.getId())
                .nextRetryAt(LocalDateTime.now())
                .lastError("retry")
                .build());

        // when
        subscribeService.retryUnsubscribe(subscribe.getId());

        // then
        assertThat(unsubscribeAgent.getRequests())
                .containsExactly(new FakeUnsubscribeAgent.UnsubscribeRequest(
                        subscribe.getUnsubscribeUrl(),
                        subscribe.getNewsletterId()
                ));
        assertThat(unsubscribeRetryRepository.findById(retry.getId())).isEmpty();
        assertThat(subscribeRepository.findById(subscribe.getId())).isEmpty();
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
        assertThat(unsubscribeAgent.getRequests())
                .containsExactly(new FakeUnsubscribeAgent.UnsubscribeRequest(unsubscribeUrl, newsletterId));
        assertThat(unsubscribeRetryRepository.findBySubscribeId(subscribeId)).isEmpty();
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
        unsubscribeAgent.failWith(new RetryableException("Server Error"));

        // when
        subscribeService.processUnsubscribe(subscribeId, newsletterId, unsubscribeUrl);

        // then
        assertThat(unsubscribeRetryRepository.findBySubscribeId(subscribeId))
                .hasValueSatisfying(retry -> {
                    assertThat(retry.getRetryCount()).isEqualTo(1);
                    assertThat(retry.getLastError()).isEqualTo("Server Error");
                });
    }

    @Test
    void 재시도_횟수가_초과되면_FAILED_상태로_변경하고_재시도_항목을_삭제한다() {
        // given
        Member member = memberRepository.save(TestFixture.uniqueMemberFixture());
        Subscribe subscribe = saveSubscribe(member);
        Long subscribeId = subscribe.getId();
        String unsubscribeUrl = subscribe.getUnsubscribeUrl();
        UnsubscribeRetry retry = UnsubscribeRetry.builder()
                .subscribeId(subscribeId)
                .nextRetryAt(LocalDateTime.now())
                .lastError("previous")
                .build();
        retry.increaseRetryCount(LocalDateTime.now(), "first");
        retry.increaseRetryCount(LocalDateTime.now(), "second");
        retry.increaseRetryCount(LocalDateTime.now(), "third");
        unsubscribeRetryRepository.save(retry);
        unsubscribeAgent.failWith(new RetryableException("Server Error"));

        // when
        subscribeService.processUnsubscribe(subscribeId, subscribe.getNewsletterId(), unsubscribeUrl);

        // then
        assertThat(unsubscribeRetryRepository.findBySubscribeId(subscribeId)).isEmpty();
        assertThat(discordNotifier.getUnsubscribeErrorNotifications())
                .containsExactly(new FakeDiscordWebhookNotifier.UnsubscribeErrorNotification(
                        "최대 재시도 횟수에 도달했습니다 : Server Error",
                        subscribeId,
                        unsubscribeUrl
                ));
        Subscribe result = subscribeRepository.findById(subscribeId).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(SubscribeStatus.UNSUBSCRIBE_FAILED);
    }

    @Test
    void 예상치_못한_에러_발생시_FAILED_상태로_변경하고_알림을_보낸다() {
        // given
        Member member = memberRepository.save(TestFixture.uniqueMemberFixture());
        Subscribe subscribe = saveSubscribe(member);
        Long subscribeId = subscribe.getId();
        String unsubscribeUrl = subscribe.getUnsubscribeUrl();
        unsubscribeAgent.failWith(new RuntimeException("boom"));

        // when
        subscribeService.processUnsubscribe(subscribeId, subscribe.getNewsletterId(), unsubscribeUrl);

        // then
        assertThat(discordNotifier.getUnsubscribeErrorNotifications())
                .containsExactly(new FakeDiscordWebhookNotifier.UnsubscribeErrorNotification(
                        "예상치 못한 예외: boom",
                        subscribeId,
                        unsubscribeUrl
                ));
        Subscribe result = subscribeRepository.findById(subscribeId).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(SubscribeStatus.UNSUBSCRIBE_FAILED);
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
        unsubscribeAgent.failWith(new AutoUnsubscribeFailedException("Invalid URL", newsletterId, unsubscribeUrl));

        // when
        subscribeService.processUnsubscribe(subscribeId, newsletterId, unsubscribeUrl);

        // then
        assertThat(discordNotifier.getUnsubscribeErrorNotifications())
                .containsExactly(new FakeDiscordWebhookNotifier.UnsubscribeErrorNotification(
                        "Invalid URL",
                        s.getId(),
                        unsubscribeUrl
                ));

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

    private Subscribe saveSubscribe(Member member) {
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
        return subscribeRepository.save(Subscribe.builder()
                .memberId(member.getId())
                .newsletterId(newsletter.getId())
                .unsubscribeUrl("https://example.com/unsub")
                .build());
    }
}
