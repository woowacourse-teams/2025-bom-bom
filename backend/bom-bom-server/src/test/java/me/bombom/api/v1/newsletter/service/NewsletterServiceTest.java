package me.bombom.api.v1.newsletter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.newsletter.domain.NewsletterPublicationStatus;
import me.bombom.api.v1.newsletter.dto.CategoryResponse;
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

    @Autowired
    private Clock clock;

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
        newsletters = TestFixture.createNewslettersWithDetails(categories, newsletterDetails);
        newsletters = newsletterRepository.saveAll(newsletters);
        
        // NewsletterSubscriptionCount 생성 및 저장
        List<NewsletterSubscriptionCount> subscriptionCounts = TestFixture.createNewsletterSubscriptionCounts(newsletters);
        newsletterSubscriptionCountRepository.saveAll(subscriptionCounts);
    }

    @Test
    void 비로그인_상태로_뉴스레터를_모두_조회할_수_있다_구독_여부는_모두_false() {
        //when
        List<NewsletterResponse> result = newsletterService.getNewsletters(null, false);

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
                                    newsletters.get(2).getId(),
                                    newsletters.get(3).getId()
                            );
            softly.assertThat(result)
                            .extracting("isSubscribed")
                            .containsOnly(false);
        });
    }

    @Test
    void 로그인_상태로_뉴스레터_목록_조회_시_구독_여부가_함께_온다() {
        //given
        Member member = TestFixture.createUniqueMember("uniqueNickname", "uniqueProviderId");
        memberRepository.save(member);
        Subscribe subscribe = TestFixture.createSubscribe(newsletters.getFirst(), member);
        subscribeRepository.save(subscribe);

        //when
        List<NewsletterResponse> result = newsletterService.getNewsletters(member.getId(), false);

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
             softly.assertThat(result.status()).isEqualTo(newsletter.getStatus());
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
            softly.assertThat(result.status()).isEqualTo(newsletter.getStatus());
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
                member.getId()
        );
        NewsletterWithDetailResponse notSubscribedResult = newsletterService.getNewsletterWithDetail(
                newsletters.getLast().getId(),
                member.getId()
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

    @Test
    void includeSuspended가_false일_때_휴재_폐간_뉴스레터는_목록에_보이지_않는다() {
        //given - 휴재 뉴스레터 생성
        NewsletterDetail suspendedDetail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(false));
        Newsletter suspendedNewsletter = newsletterRepository.save(
                Newsletter.builder()
                        .name("휴재 뉴스레터")
                        .description("설명")
                        .imageUrl("https://cdn.bombom.me/img.png")
                        .email("suspended@test.com")
                        .categoryId(categories.getFirst().getId())
                        .detailId(suspendedDetail.getId())
                        .status(NewsletterPublicationStatus.SUSPENDED)
                        .build()
        );

        //when
        List<NewsletterResponse> result = newsletterService.getNewsletters(null, false);

        //then
        assertThat(result)
                .extracting("newsletterId")
                .doesNotContain(suspendedNewsletter.getId());
    }

    @Test
    void includeSuspended가_true일_때_휴재_뉴스레터가_목록에_포함된다() {
        //given - 휴재 뉴스레터 생성
        NewsletterDetail suspendedDetail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(false));
        Newsletter suspendedNewsletter = newsletterRepository.save(
                TestFixture.createSuspendedNewsletter(
                        "휴재 뉴스레터",
                        "suspended@test.com",
                        categories.getFirst().getId(),
                        suspendedDetail.getId(),
                        null
                )
        );

        //when
        List<NewsletterResponse> result = newsletterService.getNewsletters(null, true);

        //then
        assertThat(result)
                .extracting("newsletterId")
                .contains(suspendedNewsletter.getId());
    }

    @Test
    void includeSuspended가_true일_때_최근_휴재는_포함되고_장기_휴재는_제외된다() {
        // given
        LocalDate today = LocalDate.now(clock);
        LocalDate fiveMonthsAgo = today.minusMonths(5);
        LocalDate sevenMonthsAgo = today.minusMonths(7);

        NewsletterDetail recentSuspendedDetail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(false));
        Newsletter recentSuspendedNewsletter = newsletterRepository.save(
                TestFixture.createSuspendedNewsletter(
                        "최근 휴재 뉴스레터",
                        "recent-suspended@test.com",
                        categories.getFirst().getId(),
                        recentSuspendedDetail.getId(),
                        fiveMonthsAgo
                )
        );

        NewsletterDetail longTermSuspendedDetail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(false));
        Newsletter longTermSuspendedNewsletter = newsletterRepository.save(
                TestFixture.createSuspendedNewsletter(
                        "장기 휴재 뉴스레터",
                        "long-suspended@test.com",
                        categories.getFirst().getId(),
                        longTermSuspendedDetail.getId(),
                        sevenMonthsAgo
                )
        );

        // when
        List<NewsletterResponse> result = newsletterService.getNewsletters(null, true);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result)
                    .extracting("newsletterId")
                    .contains(recentSuspendedNewsletter.getId());

            softly.assertThat(result)
                    .extracting("newsletterId")
                    .doesNotContain(longTermSuspendedNewsletter.getId());
        });
    }

    @Test
    void includeSuspended가_true여도_폐간_뉴스레터는_목록에_표시되지_않는다() {
        //given - 폐간 뉴스레터 생성
        NewsletterDetail discontinuedDetail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(false));
        Newsletter discontinuedNewsletter = newsletterRepository.save(
                Newsletter.builder()
                        .name("폐간 뉴스레터")
                        .description("설명")
                        .imageUrl("https://cdn.bombom.me/img.png")
                        .email("discontinued@test.com")
                        .categoryId(categories.getFirst().getId())
                        .detailId(discontinuedDetail.getId())
                        .status(NewsletterPublicationStatus.DISCONTINUED)
                        .build()
        );

        //when
        List<NewsletterResponse> result = newsletterService.getNewsletters(null, true);

        //then
        assertThat(result)
                .extracting("newsletterId")
                .doesNotContain(discontinuedNewsletter.getId());
    }

    @Test
    void ACTIVE_뉴스레터가_있는_카테고리만_반환된다() {
        // given - ACTIVE 뉴스레터 없이 DISCONTINUED만 있는 카테고리 추가
        Category discontinuedOnlyCategory = categoryRepository.save(
                Category.builder().name("폐간전용").build()
        );
        NewsletterDetail detail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(false));
        newsletterRepository.save(
                Newsletter.builder()
                        .name("폐간 뉴스레터")
                        .description("설명")
                        .imageUrl("https://cdn.bombom.me/img.png")
                        .email("disc@test.com")
                        .categoryId(discontinuedOnlyCategory.getId())
                        .detailId(detail.getId())
                        .status(NewsletterPublicationStatus.DISCONTINUED)
                        .build()
        );

        // when
        List<CategoryResponse> result = newsletterService.getCategories(false);

        // then
        assertThat(result).extracting("id").doesNotContain(discontinuedOnlyCategory.getId());
    }

    @Test
    void includeSuspended가_false일_때_SUSPENDED만_있는_카테고리는_제외된다() {
        // given - SUSPENDED만 있는 카테고리 추가
        Category suspendedOnlyCategory = categoryRepository.save(
                Category.builder().name("휴재전용").build()
        );
        NewsletterDetail detail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(false));
        newsletterRepository.save(
                TestFixture.createSuspendedNewsletter(
                        "휴재 뉴스레터",
                        "sus@test.com",
                        suspendedOnlyCategory.getId(),
                        detail.getId(),
                        null
                )
        );

        // when
        List<CategoryResponse> result = newsletterService.getCategories(false);

        // then
        assertThat(result).extracting("id").doesNotContain(suspendedOnlyCategory.getId());
    }

    @Test
    void includeSuspended가_true일_때_최근_휴재_뉴스레터가_있는_카테고리가_포함된다() {
        // given - 최근 휴재 뉴스레터만 있는 카테고리
        Category recentSuspendedCategory = categoryRepository.save(
                Category.builder().name("최근휴재").build()
        );
        LocalDate today = LocalDate.now(clock);
        LocalDate fiveMonthsAgo = today.minusMonths(5);
        NewsletterDetail detail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(false));
        newsletterRepository.save(
                TestFixture.createSuspendedNewsletter(
                        "최근 휴재 뉴스레터",
                        "recent@test.com",
                        recentSuspendedCategory.getId(),
                        detail.getId(),
                        fiveMonthsAgo
                )
        );

        // when
        List<CategoryResponse> result = newsletterService.getCategories(true);

        // then
        assertThat(result).extracting("id").contains(recentSuspendedCategory.getId());
    }

    @Test
    void 폐간만_있는_카테고리는_includeSuspended와_무관하게_항상_제외된다() {
        // given
        Category discontinuedOnlyCategory = categoryRepository.save(
                Category.builder().name("폐간전용").build()
        );
        NewsletterDetail detail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(false));
        newsletterRepository.save(
                Newsletter.builder()
                        .name("폐간 뉴스레터")
                        .description("설명")
                        .imageUrl("https://cdn.bombom.me/img.png")
                        .email("disc2@test.com")
                        .categoryId(discontinuedOnlyCategory.getId())
                        .detailId(detail.getId())
                        .status(NewsletterPublicationStatus.DISCONTINUED)
                        .build()
        );

        // when
        List<CategoryResponse> resultWithSuspended = newsletterService.getCategories(true);
        List<CategoryResponse> resultWithoutSuspended = newsletterService.getCategories(false);

        // then
        assertSoftly(softly -> {
            softly.assertThat(resultWithSuspended).extracting("id").doesNotContain(discontinuedOnlyCategory.getId());
            softly.assertThat(resultWithoutSuspended).extracting("id").doesNotContain(discontinuedOnlyCategory.getId());
        });
    }

    @Test
    void NewsletterSubscriptionCount가_없는_뉴스레터도_조회된다() {
        //given - NewsletterSubscriptionCount가 없는 새로운 뉴스레터 생성
        NewsletterDetail newDetail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(false));
        Newsletter newNewsletter = newsletterRepository.save(
                TestFixture.createNewsletter(
                        "새뉴스레터",
                        "new@news.com",
                        categories.getFirst().getId(),
                        newDetail.getId()
                )
        );

        //when
        List<NewsletterResponse> result = newsletterService.getNewsletters(null, false);

        //then
        assertSoftly(softly -> {
            softly.assertThat(result.size()).isEqualTo(5); // 기존 4개 + 새로 생성한 1개
            softly.assertThat(result)
                    .extracting("newsletterId")
                    .contains(newNewsletter.getId());
        });
    }
}
