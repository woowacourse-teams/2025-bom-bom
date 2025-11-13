package me.bombom.api.v1.newsletter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.newsletter.dto.NewsletterResponse;
import me.bombom.api.v1.newsletter.dto.NewsletterWithDetailResponse;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterDetailRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.api.v1.subscribe.domain.NewsletterSubscriptionCount;
import me.bombom.api.v1.subscribe.domain.Subscribe;
import me.bombom.api.v1.subscribe.repository.NewsletterSubscriptionCountRepository;
import me.bombom.api.v1.subscribe.repository.SubscribeRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class NewsletterServiceTest {

    @Autowired
    private NewsletterService newsletterService;

    @Autowired
    private NewsletterRepository newsletterRepository;

    @Autowired
    private NewsletterDetailRepository newsletterDetailRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private NewsletterSubscriptionCountRepository newsletterSubscriptionCountRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SubscribeRepository subscribeRepository;

    private List<Newsletter> newsletters;
    private List<NewsletterDetail> newsletterDetails;
    private List<Category> categories;

    @BeforeEach
    void setup() {
        newsletterRepository.deleteAllInBatch();
        newsletterDetailRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        newsletterSubscriptionCountRepository.deleteAllInBatch();

        newsletterDetails = TestFixture.createNewsletterDetails();
        newsletterDetails = newsletterDetailRepository.saveAll(newsletterDetails);
        categories = TestFixture.createCategories();
        categoryRepository.saveAll(categories);
        newsletters = List.of(
                TestFixture.createNewsletter("뉴스픽", "news@newspick.com", categories.get(0).getId(), newsletterDetails.get(0).getId()),
                TestFixture.createNewsletter("IT타임즈", "editor@ittimes.io", categories.get(1).getId(), newsletterDetails.get(1).getId()),
                TestFixture.createNewsletter("비즈레터", "biz@biz.com", categories.get(2).getId(), newsletterDetails.get(2).getId())
        );
        newsletters = newsletterRepository.saveAll(newsletters);
        
        // NewsletterSubscriptionCount 생성 및 저장
        List<NewsletterSubscriptionCount> subscriptionCounts = TestFixture.createNewsletterSubscriptionCounts(newsletters);
        newsletterSubscriptionCountRepository.saveAll(subscriptionCounts);
    }

    @Test
    void 비로그인_상태로_뉴스레터를_모두_조회할_수_있다_구독_여부는_모두_false() {
        //when
        List<NewsletterResponse> result = newsletterService.getNewsletters(null);

        System.out.println("Expected size: " + newsletters.size());
        System.out.println("Actual size: " + result.size());
        System.out.println("Result: " + result);

        //then
        assertSoftly(softly -> {
            softly.assertThat(result.size()).isEqualTo(newsletters.size());
            softly.assertThat(result)
                            .extracting("newsletterId")
                            .containsExactlyInAnyOrder(
                                    newsletters.get(0).getId(),
                                    newsletters.get(1).getId(),
                                    newsletters.get(2).getId()
                            );
            softly.assertThat(result)
                            .extracting("isSubscribed")
                            .containsOnly(false);
        });
    }

    @Test
    void 로그인_상태로_뉴스레터_목록_조회_시_구독_여부가_함께_온다() {
        //given
        Member member = TestFixture.createUniqueMember("nickname", "providerId");
        memberRepository.save(member);
        Subscribe subscribe = TestFixture.createSubscribe(newsletters.getFirst(), member);
        subscribeRepository.save(subscribe);

        //when
        List<NewsletterResponse> result = newsletterService.getNewsletters(member);

        //then
        assertSoftly(softly -> {
            softly.assertThat(result.size()).isEqualTo(newsletters.size());
            softly.assertThat(result)
                    .extracting("isSubscribed")
                    .contains(true);
        });
    }

    @Test
    void 뉴스레터_상세정보를_조회할_수_있다() {
        // given
        Newsletter newsletter = newsletters.getFirst();
        NewsletterDetail expectedDetail = newsletterDetails.stream()
                .filter(detail -> detail.getId().equals(newsletter.getDetailId()))
                .findFirst()
                .orElseThrow();

        // when
        NewsletterWithDetailResponse result = newsletterService.getNewsletterWithDetail(newsletter.getId(), null);

        // then
        Category category = categoryRepository.findById(newsletter.getCategoryId()).get();

        assertSoftly(softly -> {
             softly.assertThat(result.description()).isEqualTo(newsletter.getDescription());
             softly.assertThat(result.name()).isEqualTo(newsletter.getName());
             softly.assertThat(result.imageUrl()).isEqualTo(newsletter.getImageUrl());
             softly.assertThat(result.category()).isEqualTo(category.getName());
             softly.assertThat(result.mainPageUrl()).isEqualTo(expectedDetail.getMainPageUrl());
             softly.assertThat(result.subscribeUrl()).isEqualTo(expectedDetail.getSubscribeUrl());
             softly.assertThat(result.issueCycle()).isEqualTo(expectedDetail.getIssueCycle());
             softly.assertThat(result.previousNewsletterUrl()).isEqualTo(expectedDetail.getPreviousNewsletterUrl());
        });
    }

    @Test
    void 두_번째_뉴스레터_상세정보를_조회할_수_있다() {
        // given
        Newsletter newsletter = newsletters.get(1);
        NewsletterDetail expectedDetail = newsletterDetails.stream()
                .filter(detail -> detail.getId().equals(newsletter.getDetailId()))
                .findFirst()
                .orElseThrow();

        // when
        NewsletterWithDetailResponse result = newsletterService.getNewsletterWithDetail(newsletter.getId(), null);

        // then
        Category category = categoryRepository.findById(newsletter.getCategoryId()).get();

        assertSoftly(softly -> {
            softly.assertThat(result.name()).isEqualTo(newsletter.getName());
            softly.assertThat(result.description()).isEqualTo(newsletter.getDescription());
            softly.assertThat(result.imageUrl()).isEqualTo(newsletter.getImageUrl());
            softly.assertThat(result.category()).isEqualTo(category.getName());
            softly.assertThat(result.mainPageUrl()).isEqualTo(expectedDetail.getMainPageUrl());
            softly.assertThat(result.subscribeUrl()).isEqualTo(expectedDetail.getSubscribeUrl());
            softly.assertThat(result.issueCycle()).isEqualTo(expectedDetail.getIssueCycle());
            softly.assertThat(result.previousNewsletterUrl()).isEqualTo(expectedDetail.getPreviousNewsletterUrl());
        });
    }

    @Test
    void 존재하지_않는_뉴스레터_조회시_예외가_발생한다() {
        // given
        Long nonExistentId = 0L;

        // when & then
        assertThatThrownBy(() -> newsletterService.getNewsletterWithDetail(nonExistentId, null))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 뉴스레터는_존재하지만_뉴스레터_상세정보가_없는_경우_예외가_발생한다() {
        // given
        Newsletter newsletterWithoutDetail = Newsletter.builder()
                .name("상세정보 없는 뉴스레터")
                .description("설명")
                .imageUrl("https://example.com/image.png")
                .email("test@test.com")
                .categoryId(1L)
                .detailId(0L) // 존재하지 않는 detail ID
                .build();
        Newsletter savedNewsletter = newsletterRepository.save(newsletterWithoutDetail);

        // when & then
        assertThatThrownBy(() -> newsletterService.getNewsletterWithDetail(savedNewsletter.getId(), null))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 로그인된_상태로_조회_시_구독_여부가_온다() {
        //given
        List<NewsletterDetail> details = TestFixture.createNewsletterDetails();
        newsletterDetailRepository.saveAll(details);
        List<Newsletter> newsletters = TestFixture.createNewslettersWithDetails(categories, details);
        newsletterRepository.saveAll(newsletters);
        Member member = TestFixture.createUniqueMember("testNickname", "testProviderId");
        memberRepository.save(member);
        Subscribe subscribe = TestFixture.createSubscribe(newsletters.getFirst(), member);
        subscribeRepository.save(subscribe);

        //when
        NewsletterWithDetailResponse subscribedResult = newsletterService.getNewsletterWithDetail(
                newsletters.getFirst().getId(),
                member
        );
        NewsletterWithDetailResponse notSubscribedResult = newsletterService.getNewsletterWithDetail(
                newsletters.getLast().getId(),
                member
        );

        //then
        assertSoftly(
                softly -> {
                    assertThat(subscribedResult.isSubscribed()).isTrue();
                    assertThat(notSubscribedResult.isSubscribed()).isFalse();
                }
        );
    }

    @Test
    void 로그인되지_않은_상태로_조회_시_구독_여부가_모두_false다() {
        //given
        List<NewsletterDetail> details = TestFixture.createNewsletterDetails();
        newsletterDetailRepository.saveAll(details);
        List<Newsletter> newsletters = TestFixture.createNewslettersWithDetails(categories, details);
        newsletterRepository.saveAll(newsletters);

        //when
        NewsletterWithDetailResponse subscribedResult = newsletterService.getNewsletterWithDetail(
                newsletters.getFirst().getId(),
                null
        );

        //then
        assertSoftly(
                softly -> {
                    assertThat(subscribedResult.isSubscribed()).isFalse();
                }
        );
    }
}
