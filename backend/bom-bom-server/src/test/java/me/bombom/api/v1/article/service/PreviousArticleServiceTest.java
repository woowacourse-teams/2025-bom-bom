package me.bombom.api.v1.article.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.domain.PreviousArticle;
import me.bombom.api.v1.article.dto.request.PreviousArticleRequest;
import me.bombom.api.v1.article.dto.response.PreviousArticleDetailResponse;
import me.bombom.api.v1.article.dto.response.PreviousArticleResponse;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.article.repository.PreviousArticleRepository;
import me.bombom.api.v1.article.service.strategy.PreviousArticleStrategy;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.newsletter.domain.NewsletterPreviousPolicy;
import me.bombom.api.v1.newsletter.domain.NewsletterPreviousStrategy;
import me.bombom.api.v1.newsletter.domain.PreviousArticleSource;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterDetailRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterPreviousPolicyRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.api.v1.subscribe.domain.Subscribe;
import me.bombom.api.v1.subscribe.repository.SubscribeRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

@IntegrationTest
class PreviousArticleServiceTest {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2025, 7, 15, 10, 0);

    @Autowired
    private NewsletterRepository newsletterRepository;

    @Autowired
    private NewsletterDetailRepository newsletterDetailRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NewsletterPreviousPolicyRepository newsletterPreviousPolicyRepository;

    @Autowired
    private PreviousArticleRepository previousArticleRepository;

    @Autowired
    private List<PreviousArticleStrategy> previousArticleStrategies;

    @Autowired
    private PreviousArticleService previousArticleService;

    @Autowired
    private SubscribeRepository subscribeRepository;

    List<Category> categories;
    List<Newsletter> newsletters;
    Member admin;
    Member normalMember;
    Newsletter testNewsletter;
    List<PreviousArticle> fixedArticles;
    List<Article> adminArticles;

    @BeforeEach
    public void setup() {
        subscribeRepository.deleteAllInBatch();
        newsletterRepository.deleteAllInBatch();
        articleRepository.deleteAllInBatch();
        previousArticleRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        newsletterPreviousPolicyRepository.deleteAllInBatch();

        // 관리자 멤버 생성
        admin = TestFixture.createUniqueMember("지난아티클관리자", "prev-admin");
        memberRepository.save(admin);

        // 일반 회원 생성
        normalMember = TestFixture.createUniqueMember("일반사용자", "normal-user");
        memberRepository.save(normalMember);

        // 서비스와 전략들의 ADMIN_ID 오버라이드
        ReflectionTestUtils.setField(previousArticleService, "PREVIOUS_ARTICLE_ADMIN_ID", admin.getId());
        previousArticleStrategies.forEach(strategy -> {
            try {
                ReflectionTestUtils.setField(strategy, "PREVIOUS_ARTICLE_ADMIN_ID", admin.getId());
            } catch (IllegalArgumentException e) {
                // PREVIOUS_ARTICLE_ADMIN_ID 필드가 없는 전략은 무시
            }
        });

        // 카테고리 생성
        categories = TestFixture.createCategories();
        categoryRepository.saveAll(categories);

        // 테스트용 뉴스레터 생성
        NewsletterDetail detail = TestFixture.createNewsletterDetail(true);
        newsletterDetailRepository.save(detail);
        testNewsletter = TestFixture.createNewsletter("테스트뉴스", "test@bombom.news", categories.getFirst().getId(), detail.getId());
        newsletterRepository.save(testNewsletter);

        // 기본 뉴스레터들 생성
        List<NewsletterDetail> newsletterDetails = TestFixture.createNewsletterDetails();
        newsletterDetailRepository.saveAll(newsletterDetails);
        newsletters = TestFixture.createNewslettersWithDetails(categories, newsletterDetails);
        newsletterRepository.saveAll(newsletters);

        // 고정 아티클 생성 (3개)
        fixedArticles = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            fixedArticles.add(TestFixture.createPreviousArticle(
                    "고정 아티클 " + i,
                    testNewsletter.getId(),
                    BASE_TIME.minusDays(10 + i)
            ));
        }
        previousArticleRepository.saveAll(fixedArticles);

        // 관리자 아티클 생성 (5개)
        adminArticles = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            adminArticles.add(TestFixture.createArticle(
                    "관리자 아티클 " + i,
                    admin.getId(),
                    testNewsletter.getId(),
                    BASE_TIME.minusDays(i)
            ));
        }
        articleRepository.saveAll(adminArticles);
    }

    @Test
    void 관리자_아티클_정리_테스트() {
        // given
        Newsletter newsletter1 = newsletters.getFirst();
        List<Article> adminArticles = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            Article article = TestFixture.createArticle(
                    "관리자 아티클 " + i,
                    admin.getId(),
                    newsletter1.getId(),
                    BASE_TIME.minusDays(i)
            );
            adminArticles.add(article);
        }

        articleRepository.saveAll(adminArticles);

        // when
        int deletedCount = previousArticleService.cleanupOldPreviousArticles();

        // then
        assertSoftly(softly -> {
            softly.assertThat(deletedCount).isEqualTo(2);

            // 뉴스레터1에 10개만 남아있는지 확인
            long newsletter1Count = articleRepository.findAll().stream()
                    .filter(article ->
                            article.getMemberId().equals(admin.getId()) &&
                                    article.getNewsletterId().equals(newsletter1.getId()))
                    .count();
            softly.assertThat(newsletter1Count).isEqualTo(10);
        });
    }

    @Test
    void 정책이_없으면_빈_목록을_반환한다() {
        // given
        Newsletter newsletter = newsletters.getFirst();
        PreviousArticleRequest request = new PreviousArticleRequest(newsletter.getId(), 5);

        // when
        List<PreviousArticleResponse> result = previousArticleService.getPreviousArticles(request);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 정책에_맞는_LATEST_ONLY_전략을_사용해_아티클을_반환한다() {
        // given
        NewsletterPreviousPolicy policy = TestFixture.createNewsletterPreviousPolicy(
                testNewsletter.getId(), NewsletterPreviousStrategy.LATEST_ONLY, 3, 0);
        newsletterPreviousPolicyRepository.save(policy);

        PreviousArticleRequest request = new PreviousArticleRequest(testNewsletter.getId(), 3);

        // when
        List<PreviousArticleResponse> result = previousArticleService.getPreviousArticles(request);

        // then
        List<Long> expectedIds = adminArticles.stream()
                .sorted((a, b) -> b.getArrivedDateTime().compareTo(a.getArrivedDateTime()))
                .skip(1) // LATEST_ONLY 전략은 가장 최신 1개를 제외
                .limit(3)
                .map(Article::getId)
                .toList();

        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(3);
            softly.assertThat(result.stream().map(PreviousArticleResponse::id).toList())
                    .containsExactlyElementsOf(expectedIds);
        });
    }

    @Test
    void 정책이_INACTIVE이면_빈_목록을_반환한다() {
        // given
        NewsletterPreviousPolicy policy = TestFixture.createNewsletterPreviousPolicy(
                testNewsletter.getId(), NewsletterPreviousStrategy.INACTIVE, 5, 0);
        newsletterPreviousPolicyRepository.save(policy);
        PreviousArticleRequest request = new PreviousArticleRequest(testNewsletter.getId(), 5);

        // when
        List<PreviousArticleResponse> result = previousArticleService.getPreviousArticles(request);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 정책에_맞는_FIXED_ONLY_전략을_사용해_고정_아티클만_반환한다() {
        // given
        NewsletterPreviousPolicy policy = TestFixture.createNewsletterPreviousPolicy(
                testNewsletter.getId(), NewsletterPreviousStrategy.FIXED_ONLY, 3, 3);
        newsletterPreviousPolicyRepository.save(policy);

        PreviousArticleRequest request = new PreviousArticleRequest(testNewsletter.getId(), 3);

        // when
        List<PreviousArticleResponse> result = previousArticleService.getPreviousArticles(request);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(3);
            softly.assertThat(result.stream().map(PreviousArticleResponse::id).toList())
                    .containsExactlyElementsOf(fixedArticles.stream().map(PreviousArticle::getId).toList());
        });
    }

    @Test
    void 정책에_맞는_FIXED_WITH_LATEST_전략을_사용해_고정_아티클과_최신_아티클을_반환한다() {
        // given
        // totalCount=5, fixedCount=2 이므로 고정 2개 + 최신 3개(단, 가장 최신 1개 제외)
        NewsletterPreviousPolicy policy = TestFixture.createNewsletterPreviousPolicy(
                testNewsletter.getId(), NewsletterPreviousStrategy.FIXED_WITH_LATEST, 5, 2);
        newsletterPreviousPolicyRepository.save(policy);

        PreviousArticleRequest request = new PreviousArticleRequest(testNewsletter.getId(), 5);

        // when
        List<PreviousArticleResponse> result = previousArticleService.getPreviousArticles(request);

        // then
        List<Long> expectedFixedIds = fixedArticles.stream()
                .limit(2)
                .map(PreviousArticle::getId)
                .toList();

        List<Long> expectedLatestIds = adminArticles.stream()
                .sorted((a, b) -> b.getArrivedDateTime().compareTo(a.getArrivedDateTime()))
                .skip(1) // 가장 최신 1개 제외
                .limit(3)
                .map(Article::getId)
                .toList();

        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(5);
            // 고정 아티클이 포함되어 있는지 확인
            softly.assertThat(result.stream().map(PreviousArticleResponse::id).toList())
                    .containsAll(expectedFixedIds);
            // 최신 아티클도 포함되어 있는지 확인
            softly.assertThat(result.stream().map(PreviousArticleResponse::id).toList())
                    .containsAll(expectedLatestIds);
        });
    }

    @Test
    void FIXED_ONLY_전략에서_고정_아티클_개수가_설정과_다르면_실제_개수만큼_반환한다() {
        // given
        // BeforeEach에서 3개 생성했지만, 설정은 5개로 설정
        NewsletterPreviousPolicy policy = TestFixture.createNewsletterPreviousPolicy(
                testNewsletter.getId(), NewsletterPreviousStrategy.FIXED_ONLY, 5, 5);
        newsletterPreviousPolicyRepository.save(policy);

        PreviousArticleRequest request = new PreviousArticleRequest(testNewsletter.getId(), 5);

        // when
        List<PreviousArticleResponse> result = previousArticleService.getPreviousArticles(request);

        // then
        assertThat(result).hasSize(3); // 실제로는 3개만 존재
    }

    @Test
    void FIXED_소스_로그인_사용자_구독중_상세_조회() {
        // given
        NewsletterPreviousPolicy policy = TestFixture.createNewsletterPreviousPolicy(
                testNewsletter.getId(), NewsletterPreviousStrategy.FIXED_ONLY, 3, 3, 80);
        newsletterPreviousPolicyRepository.save(policy);

        Subscribe subscribe = TestFixture.createSubscribe(testNewsletter, normalMember);
        subscribeRepository.save(subscribe);

        PreviousArticle targetArticle = fixedArticles.getFirst();

        // when
        PreviousArticleDetailResponse result = previousArticleService.getPreviousArticleDetail(
                targetArticle.getId(), PreviousArticleSource.FIXED, normalMember);

        // then
        assertThat(result.title()).isEqualTo(targetArticle.getTitle());
    }

    @Test
    void FIXED_소스_로그인_사용자_구독안함_상세_조회() {
        // given
        NewsletterPreviousPolicy policy = TestFixture.createNewsletterPreviousPolicy(
                testNewsletter.getId(), NewsletterPreviousStrategy.FIXED_ONLY, 3, 3, 80);
        newsletterPreviousPolicyRepository.save(policy);

        PreviousArticle targetArticle = fixedArticles.getFirst();

        // when
        PreviousArticleDetailResponse result = previousArticleService.getPreviousArticleDetail(
                targetArticle.getId(), PreviousArticleSource.FIXED, normalMember);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.title()).isEqualTo(targetArticle.getTitle());
            softly.assertThat(result.isSubscribed()).isFalse();
        });
    }

    @Test
    void FIXED_소스_비로그인_사용자_상세_조회() {
        // given
        NewsletterPreviousPolicy policy = TestFixture.createNewsletterPreviousPolicy(
                testNewsletter.getId(), NewsletterPreviousStrategy.FIXED_ONLY, 3, 3, 80);
        newsletterPreviousPolicyRepository.save(policy);

        PreviousArticle targetArticle = fixedArticles.getFirst();

        // when
        PreviousArticleDetailResponse result = previousArticleService.getPreviousArticleDetail(
                targetArticle.getId(), PreviousArticleSource.FIXED, null);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.title()).isEqualTo(targetArticle.getTitle());
            softly.assertThat(result.isSubscribed()).isFalse();
        });
    }

    @Test
    void LATEST_소스_로그인_사용자_구독중_상세_조회() {
        // given
        NewsletterPreviousPolicy policy = TestFixture.createNewsletterPreviousPolicy(
                testNewsletter.getId(), NewsletterPreviousStrategy.LATEST_ONLY, 3, 0, 90);
        newsletterPreviousPolicyRepository.save(policy);

        Subscribe subscribe = TestFixture.createSubscribe(testNewsletter, normalMember);
        subscribeRepository.save(subscribe);

        Article targetArticle = adminArticles.getFirst();

        // when
        PreviousArticleDetailResponse result = previousArticleService.getPreviousArticleDetail(
                targetArticle.getId(), PreviousArticleSource.LATEST, normalMember);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.title()).isEqualTo(targetArticle.getTitle());
            softly.assertThat(result.isSubscribed()).isTrue();
        });
    }

    @Test
    void LATEST_소스_로그인_사용자_구독안함_상세_조회() {
        // given
        NewsletterPreviousPolicy policy = TestFixture.createNewsletterPreviousPolicy(
                testNewsletter.getId(), NewsletterPreviousStrategy.LATEST_ONLY, 3, 0, 90);
        newsletterPreviousPolicyRepository.save(policy);

        Article targetArticle = adminArticles.getFirst();

        // when
        PreviousArticleDetailResponse result = previousArticleService.getPreviousArticleDetail(
                targetArticle.getId(), PreviousArticleSource.LATEST, normalMember);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.title()).isEqualTo(targetArticle.getTitle());
            softly.assertThat(result.isSubscribed()).isFalse();
        });
    }

    @Test
    void LATEST_소스_비로그인_사용자_상세_조회() {
        // given
        NewsletterPreviousPolicy policy = TestFixture.createNewsletterPreviousPolicy(
                testNewsletter.getId(), NewsletterPreviousStrategy.LATEST_ONLY, 3, 0, 90);
        newsletterPreviousPolicyRepository.save(policy);

        Article targetArticle = adminArticles.getFirst();

        // when
        PreviousArticleDetailResponse result = previousArticleService.getPreviousArticleDetail(
                targetArticle.getId(), PreviousArticleSource.LATEST, null);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.title()).isEqualTo(targetArticle.getTitle());
            softly.assertThat(result.isSubscribed()).isFalse();
        });
    }

    @Test
    void FIXED_소스_존재하지_않는_아티클_조회시_예외() {
        // given
        Long invalidArticleId = 99999L;

        // when & then
        assertThatThrownBy(() -> previousArticleService.getPreviousArticleDetail(
                invalidArticleId, PreviousArticleSource.FIXED, normalMember))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void LATEST_소스_존재하지_않는_아티클_조회시_예외() {
        // given
        Long invalidArticleId = 99999L;

        // when & then
        assertThatThrownBy(() -> previousArticleService.getPreviousArticleDetail(
                invalidArticleId, PreviousArticleSource.LATEST, normalMember))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void FIXED_소스_INACTIVE_정책인_경우_조회_실패() {
        // given
        NewsletterPreviousPolicy policy = TestFixture.createNewsletterPreviousPolicy(
                testNewsletter.getId(), NewsletterPreviousStrategy.INACTIVE, 0, 0, 0);
        newsletterPreviousPolicyRepository.save(policy);

        PreviousArticle targetArticle = fixedArticles.getFirst();

        // when & then - INACTIVE 정책이면 조회 불가
        assertThatThrownBy(() -> previousArticleService.getPreviousArticleDetail(
                targetArticle.getId(), PreviousArticleSource.FIXED, normalMember))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void FIXED_소스_LATEST_ONLY_정책인_경우_조회_실패() {
        // given
        NewsletterPreviousPolicy policy = TestFixture.createNewsletterPreviousPolicy(
                testNewsletter.getId(), NewsletterPreviousStrategy.LATEST_ONLY, 3, 0, 80);
        newsletterPreviousPolicyRepository.save(policy);

        PreviousArticle targetArticle = fixedArticles.getFirst();

        // when & then - LATEST_ONLY 정책이면 FIXED 아티클 조회 불가
        assertThatThrownBy(() -> previousArticleService.getPreviousArticleDetail(
                targetArticle.getId(), PreviousArticleSource.FIXED, normalMember))
                .isInstanceOf(CIllegalArgumentException.class);
    }
}
