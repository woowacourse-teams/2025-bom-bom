package me.bombom.api.v1.article.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
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
        ReflectionTestUtils.setField(previousArticleService, "previousArticleAdminId", admin.getId());
        previousArticleStrategies.forEach(strategy -> {
            try {
                ReflectionTestUtils.setField(strategy, "previousArticleAdminId", admin.getId());
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
    void previous_article_자동이동_아티클_정리_테스트() {
        // given
        Newsletter newsletter1 = newsletters.getFirst();
        
        // 자동 이동된 아티클(isFixed=false) 12개 생성
        List<PreviousArticle> autoMovedArticles = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            PreviousArticle article = PreviousArticle.builder()
                    .title("자동 이동 아티클 " + i)
                    .contents("<h1>내용</h1>")
                    .contentsSummary("요약")
                    .expectedReadTime(5)
                    .newsletterId(newsletter1.getId())
                    .arrivedDateTime(BASE_TIME.minusDays(i))
                    .isFixed(false)  // 자동 이동
                    .build();
            autoMovedArticles.add(article);
        }
        previousArticleRepository.saveAll(autoMovedArticles);
        
        // 고정 아티클(isFixed=true) 3개 추가 (이건 삭제 안됨)
        List<PreviousArticle> fixedArticles = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            PreviousArticle article = PreviousArticle.builder()
                    .title("고정 아티클 " + i)
                    .contents("<h1>내용</h1>")
                    .contentsSummary("요약")
                    .expectedReadTime(5)
                    .newsletterId(newsletter1.getId())
                    .arrivedDateTime(BASE_TIME.minusDays(20 + i))
                    .isFixed(true)  // 고정
                    .build();
            fixedArticles.add(article);
        }
        previousArticleRepository.saveAll(fixedArticles);

        // when
        int deletedCount = previousArticleService.cleanupOldPreviousArticles();

        // then
        assertSoftly(softly -> {
            // 자동 이동 12개 중 2개 삭제됨 (최신 10개만 유지)
            softly.assertThat(deletedCount).isEqualTo(2);

            // 자동 이동 아티클은 10개만 남아야 함
            long autoMovedCount = previousArticleRepository.findAll().stream()
                    .filter(pa -> pa.getNewsletterId().equals(newsletter1.getId()))
                    .filter(pa -> !pa.isFixed())
                    .count();
            softly.assertThat(autoMovedCount).isEqualTo(10);
            
            // 고정 아티클은 그대로 3개 유지
            long fixedCount = previousArticleRepository.findAll().stream()
                    .filter(pa -> pa.getNewsletterId().equals(newsletter1.getId()))
                    .filter(PreviousArticle::isFixed)
                    .count();
            softly.assertThat(fixedCount).isEqualTo(3);
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
        // 자동 이동된 아티클 (isFixed=false) 생성
        // arrivedDateTime: BASE_TIME, -1일, -2일, -3일, -4일
        List<PreviousArticle> autoMovedArticles = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            autoMovedArticles.add(PreviousArticle.builder()
                    .title("자동 이동 아티클 " + i)
                    .contents("<h1>내용</h1>")
                    .contentsSummary("요약")
                    .expectedReadTime(5)
                    .newsletterId(testNewsletter.getId())
                    .arrivedDateTime(BASE_TIME.minusDays(i))
                    .isFixed(false)
                    .build());
        }
        previousArticleRepository.saveAll(autoMovedArticles);
        
        NewsletterPreviousPolicy policy = TestFixture.createNewsletterPreviousPolicy(
                testNewsletter.getId(), NewsletterPreviousStrategy.LATEST_ONLY, 3, 0);
        newsletterPreviousPolicyRepository.save(policy);

        PreviousArticleRequest request = new PreviousArticleRequest(testNewsletter.getId(), 3);

        // when
        List<PreviousArticleResponse> result = previousArticleService.getPreviousArticles(request);

        // then
        // previous_article 중 최신(BASE_TIME) 1개 제외하고 나머지 중 3개
        // 예상: -1일, -2일, -3일 (arrivedDateTime DESC 정렬)
        List<Long> expectedIds = autoMovedArticles.stream()
                .sorted((a, b) -> b.getArrivedDateTime().compareTo(a.getArrivedDateTime()))
                .skip(1) // 최신 1개(BASE_TIME) 제외
                .limit(3)
                .map(PreviousArticle::getId)
                .toList();

        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(3);
            softly.assertThat(result.stream().map(PreviousArticleResponse::articleId).toList())
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
            softly.assertThat(result.stream().map(PreviousArticleResponse::articleId).toList())
                    .containsExactlyElementsOf(fixedArticles.stream().map(PreviousArticle::getId).toList());
        });
    }

    @Test
    void 정책에_맞는_FIXED_WITH_LATEST_전략을_사용해_고정_아티클과_최신_아티클을_반환한다() {
        // given
        // 자동 이동된 아티클 (isFixed=false) 생성
        // arrivedDateTime: BASE_TIME, -1일, -2일, -3일, -4일
        List<PreviousArticle> autoMovedArticles = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            autoMovedArticles.add(PreviousArticle.builder()
                    .title("자동 이동 아티클 " + i)
                    .contents("<h1>내용</h1>")
                    .contentsSummary("요약")
                    .expectedReadTime(5)
                    .newsletterId(testNewsletter.getId())
                    .arrivedDateTime(BASE_TIME.minusDays(i))
                    .isFixed(false)
                    .build());
        }
        previousArticleRepository.saveAll(autoMovedArticles);
        
        // totalCount=5, fixedCount=2 이므로 고정 2개 + 자동 이동 3개
        NewsletterPreviousPolicy policy = TestFixture.createNewsletterPreviousPolicy(
                testNewsletter.getId(), NewsletterPreviousStrategy.FIXED_WITH_LATEST, 3, 2);
        newsletterPreviousPolicyRepository.save(policy);

        PreviousArticleRequest request = new PreviousArticleRequest(testNewsletter.getId(), 5);

        // when
        List<PreviousArticleResponse> result = previousArticleService.getPreviousArticles(request);

        // then
        // 예상 순서: 고정 2개 (arrivedDateTime DESC) + 자동 3개 (arrivedDateTime DESC, 최신 제외)
        List<Long> expectedFixedIds = fixedArticles.stream()
                .limit(2)
                .map(PreviousArticle::getId)
                .toList();

        List<Long> expectedAutoMovedIds = autoMovedArticles.stream()
                .sorted((a, b) -> b.getArrivedDateTime().compareTo(a.getArrivedDateTime()))
                .skip(1) // 최신 1개(BASE_TIME) 제외
                .limit(3)
                .map(PreviousArticle::getId)
                .toList();

        // 결과: [고정0, 고정1, 자동1, 자동2, 자동3]
        List<Long> expectedOrder = Stream.concat(
                expectedFixedIds.stream(),
                expectedAutoMovedIds.stream()
        ).toList();

        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(5);
            softly.assertThat(result.stream().map(PreviousArticleResponse::articleId).toList())
                    .containsExactlyElementsOf(expectedOrder);
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
    void 로그인_구독중_사용자의_지난_아티클_상세_조회() {
        // given
        NewsletterPreviousPolicy policy = TestFixture.createNewsletterPreviousPolicy(
                testNewsletter.getId(), NewsletterPreviousStrategy.FIXED_ONLY, 3, 3, 80);
        newsletterPreviousPolicyRepository.save(policy);

        Subscribe subscribe = TestFixture.createSubscribe(testNewsletter, normalMember);
        subscribeRepository.save(subscribe);

        PreviousArticle targetArticle = fixedArticles.getFirst();

        // when
        PreviousArticleDetailResponse result = previousArticleService.getPreviousArticleDetail(
                targetArticle.getId(), normalMember);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.title()).isEqualTo(targetArticle.getTitle());
            softly.assertThat(result.isSubscribed()).isTrue();
        });
    }

    @Test
    void 로그인_구독안함_사용자의_지난_아티클_상세_조회() {
        // given
        NewsletterPreviousPolicy policy = TestFixture.createNewsletterPreviousPolicy(
                testNewsletter.getId(), NewsletterPreviousStrategy.FIXED_ONLY, 3, 3, 80);
        newsletterPreviousPolicyRepository.save(policy);

        PreviousArticle targetArticle = fixedArticles.getFirst();

        // when
        PreviousArticleDetailResponse result = previousArticleService.getPreviousArticleDetail(
                targetArticle.getId(), normalMember);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.title()).isEqualTo(targetArticle.getTitle());
            softly.assertThat(result.isSubscribed()).isFalse();
        });
    }

    @Test
    void 비로그인_사용자의_지난_아티클_상세_조회() {
        // given
        NewsletterPreviousPolicy policy = TestFixture.createNewsletterPreviousPolicy(
                testNewsletter.getId(), NewsletterPreviousStrategy.FIXED_ONLY, 3, 3, 80);
        newsletterPreviousPolicyRepository.save(policy);

        PreviousArticle targetArticle = fixedArticles.getFirst();

        // when
        PreviousArticleDetailResponse result = previousArticleService.getPreviousArticleDetail(
                targetArticle.getId(), null);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.title()).isEqualTo(targetArticle.getTitle());
            softly.assertThat(result.isSubscribed()).isFalse();
        });
    }

    @Test
    void 존재하지_않는_지난_아티클_조회시_예외() {
        // given
        Long invalidArticleId = 99999L;

        // when & then
        assertThatThrownBy(() -> previousArticleService.getPreviousArticleDetail(
                invalidArticleId, normalMember))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void INACTIVE_정책인_경우_지난_아티클_상세_조회_실패() {
        // given
        NewsletterPreviousPolicy policy = TestFixture.createNewsletterPreviousPolicy(
                testNewsletter.getId(), NewsletterPreviousStrategy.INACTIVE, 0, 0, 0);
        newsletterPreviousPolicyRepository.save(policy);

        PreviousArticle targetArticle = fixedArticles.getFirst();

        // when & then
        assertThatThrownBy(() -> previousArticleService.getPreviousArticleDetail(
                targetArticle.getId(), normalMember))
                .isInstanceOf(CIllegalArgumentException.class);
    }

}
