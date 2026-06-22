package me.bombom.api.v1.article.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
import me.bombom.api.v1.common.exception.CServerErrorException;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.domain.Role;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.member.repository.RoleRepository;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void previous_article_자동이동_아티클_정리_테스트() {
        // given
        Newsletter newsletter = 지난아티클뉴스레터저장("지난아티클정리");
        
        자동이동지난아티클저장(newsletter.getId(), 12);
        고정지난아티클저장(newsletter.getId(), 3);

        // when
        int deletedCount = previousArticleService.cleanupOldPreviousArticles();

        // then
        assertSoftly(softly -> {
            // 자동 이동 12개 중 2개 삭제됨 (최신 10개만 유지)
            softly.assertThat(deletedCount).isEqualTo(2);

            // 자동 이동 아티클은 10개만 남아야 함
            long autoMovedCount = previousArticleRepository.findAll().stream()
                    .filter(pa -> pa.getNewsletterId().equals(newsletter.getId()))
                    .filter(pa -> !pa.isFixed())
                    .count();
            softly.assertThat(autoMovedCount).isEqualTo(10);
            
            // 고정 아티클은 그대로 3개 유지
            long fixedCount = previousArticleRepository.findAll().stream()
                    .filter(pa -> pa.getNewsletterId().equals(newsletter.getId()))
                    .filter(PreviousArticle::isFixed)
                    .count();
            softly.assertThat(fixedCount).isEqualTo(3);
        });
    }

    @Test
    void 정책이_없으면_빈_목록을_반환한다() {
        // given
        Newsletter newsletter = 지난아티클뉴스레터저장("정책없음");
        PreviousArticleRequest request = new PreviousArticleRequest(newsletter.getId(), 5);

        // when
        List<PreviousArticleResponse> result = previousArticleService.getPreviousArticles(request);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 정책에_맞는_LATEST_ONLY_전략을_사용해_아티클을_반환한다() {
        // given
        Newsletter newsletter = 지난아티클뉴스레터저장("최근전략");
        List<PreviousArticle> autoMovedArticles = 자동이동지난아티클저장(newsletter.getId(), 5);
        
        지난아티클정책저장(newsletter, NewsletterPreviousStrategy.RECENT_ONLY, 3, 0);

        PreviousArticleRequest request = new PreviousArticleRequest(newsletter.getId(), 3);

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
        Newsletter newsletter = 지난아티클뉴스레터저장("비활성전략");
        지난아티클정책저장(newsletter, NewsletterPreviousStrategy.INACTIVE, 5, 0);
        PreviousArticleRequest request = new PreviousArticleRequest(newsletter.getId(), 5);

        // when
        List<PreviousArticleResponse> result = previousArticleService.getPreviousArticles(request);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 정책에_맞는_FIXED_ONLY_전략을_사용해_고정_아티클만_반환한다() {
        // given
        Newsletter newsletter = 지난아티클뉴스레터저장("고정전략");
        List<PreviousArticle> fixedArticles = 고정지난아티클저장(newsletter.getId(), 3);
        지난아티클정책저장(newsletter, NewsletterPreviousStrategy.FIXED_ONLY, 3, 3);

        PreviousArticleRequest request = new PreviousArticleRequest(newsletter.getId(), 3);

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
        Newsletter newsletter = 지난아티클뉴스레터저장("고정최근전략");
        List<PreviousArticle> fixedArticles = 고정지난아티클저장(newsletter.getId(), 3);
        List<PreviousArticle> autoMovedArticles = 자동이동지난아티클저장(newsletter.getId(), 5);
        
        // totalCount=5, fixedCount=2 이므로 고정 2개 + 자동 이동 3개
        지난아티클정책저장(newsletter, NewsletterPreviousStrategy.FIXED_WITH_RECENT, 3, 2);

        PreviousArticleRequest request = new PreviousArticleRequest(newsletter.getId(), 5);

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
        Newsletter newsletter = 지난아티클뉴스레터저장("고정개수부족");
        고정지난아티클저장(newsletter.getId(), 3);

        // BeforeEach에서 3개 생성했지만, 설정은 5개로 설정
        지난아티클정책저장(newsletter, NewsletterPreviousStrategy.FIXED_ONLY, 5, 5);

        PreviousArticleRequest request = new PreviousArticleRequest(newsletter.getId(), 5);

        // when
        List<PreviousArticleResponse> result = previousArticleService.getPreviousArticles(request);

        // then
        assertThat(result).hasSize(3); // 실제로는 3개만 존재
    }

    @Test
    void 로그인_구독중_사용자의_지난_아티클_상세_조회() {
        // given
        Newsletter newsletter = 지난아티클뉴스레터저장("구독상세조회");
        Member normalMember = 일반회원저장();
        List<PreviousArticle> fixedArticles = 고정지난아티클저장(newsletter.getId(), 3);
        지난아티클정책저장(newsletter, NewsletterPreviousStrategy.FIXED_ONLY, 3, 3, 80);

        Subscribe subscribe = TestFixture.createSubscribe(newsletter, normalMember);
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
        Newsletter newsletter = 지난아티클뉴스레터저장("미구독상세조회");
        Member normalMember = 일반회원저장();
        List<PreviousArticle> fixedArticles = 고정지난아티클저장(newsletter.getId(), 3);
        지난아티클정책저장(newsletter, NewsletterPreviousStrategy.FIXED_ONLY, 3, 3, 80);

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
        Newsletter newsletter = 지난아티클뉴스레터저장("비로그인상세조회");
        List<PreviousArticle> fixedArticles = 고정지난아티클저장(newsletter.getId(), 3);
        지난아티클정책저장(newsletter, NewsletterPreviousStrategy.FIXED_ONLY, 3, 3, 80);

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
        Member normalMember = 일반회원저장();

        // when & then
        assertThatThrownBy(() -> previousArticleService.getPreviousArticleDetail(
                invalidArticleId, normalMember))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void INACTIVE_정책인_경우_지난_아티클_상세_조회_실패() {
        // given
        Newsletter newsletter = 지난아티클뉴스레터저장("비활성상세조회");
        Member normalMember = 일반회원저장();
        List<PreviousArticle> fixedArticles = 고정지난아티클저장(newsletter.getId(), 1);
        지난아티클정책저장(newsletter, NewsletterPreviousStrategy.INACTIVE, 0, 0, 0);

        PreviousArticle targetArticle = fixedArticles.getFirst();

        // when & then
        assertThatThrownBy(() -> previousArticleService.getPreviousArticleDetail(
                targetArticle.getId(), normalMember))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void moveAdminArticles_성공_Archive권한_멤버_존재() {
        // given
        Member admin = 아카이브관리자저장();
        Newsletter newsletter = 지난아티클뉴스레터저장("관리자아티클이동");
        관리자아티클저장(admin, newsletter, 5);
        long initialCount = previousArticleRepository.count();

        // when
        previousArticleService.moveAdminArticles();

        // then
        long finalCount = previousArticleRepository.count();
        assertThat(finalCount).isGreaterThan(initialCount);
    }

    @Test
    void moveAdminArticles_실패_Archive권한_멤버_없음() {
        // given

        // when & then
        assertThatThrownBy(() -> previousArticleService.moveAdminArticles())
                .isInstanceOf(CServerErrorException.class);
    }

    private Newsletter 지난아티클뉴스레터저장(String name) {
        Category category = categoryRepository.save(Category.builder()
                .name(name + "카테고리")
                .build());
        NewsletterDetail detail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(true));
        Newsletter newsletter = TestFixture.createNewsletter(
                name,
                uniqueEmail("previous"),
                category.getId(),
                detail.getId()
        );
        return newsletterRepository.save(newsletter);
    }

    private Member 일반회원저장() {
        Role memberRole = roleRepository.save(Role.builder().authority("MEMBER").build());
        Member member = TestFixture.createMemberWithRole("일반사용자", uniqueValue("normal"), memberRole.getId());
        return memberRepository.save(member);
    }

    private Member 아카이브관리자저장() {
        Role archiveRole = roleRepository.save(Role.builder().authority("ARCHIVE").build());
        Member admin = TestFixture.createMemberWithRole("지난아티클관리자", uniqueValue("archive"), archiveRole.getId());
        return memberRepository.save(admin);
    }

    private List<PreviousArticle> 고정지난아티클저장(Long newsletterId, int count) {
        List<PreviousArticle> articles = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            articles.add(TestFixture.createPreviousArticle(
                    "고정 아티클 " + i,
                    newsletterId,
                    BASE_TIME.minusDays(10 + i)
            ));
        }
        return previousArticleRepository.saveAll(articles);
    }

    private List<PreviousArticle> 자동이동지난아티클저장(Long newsletterId, int count) {
        List<PreviousArticle> articles = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            articles.add(자동이동지난아티클(newsletterId, i));
        }
        return previousArticleRepository.saveAll(articles);
    }

    private PreviousArticle 자동이동지난아티클(Long newsletterId, int index) {
        return PreviousArticle.builder()
                .title("자동 이동 아티클 " + index)
                .contents("<h1>내용</h1>")
                .contentsSummary("요약")
                .expectedReadTime(5)
                .newsletterId(newsletterId)
                .arrivedDateTime(BASE_TIME.minusDays(index))
                .isFixed(false)
                .build();
    }

    private void 관리자아티클저장(Member admin, Newsletter newsletter, int count) {
        List<Article> articles = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            articles.add(TestFixture.createArticle(
                    "관리자 아티클 " + i,
                    admin.getId(),
                    newsletter.getId(),
                    BASE_TIME.minusDays(i)
            ));
        }
        articleRepository.saveAll(articles);
    }

    private void 지난아티클정책저장(
            Newsletter newsletter,
            NewsletterPreviousStrategy strategy,
            int recentCount,
            int fixedCount
    ) {
        지난아티클정책저장(newsletter, strategy, recentCount, fixedCount, 100);
    }

    private void 지난아티클정책저장(
            Newsletter newsletter,
            NewsletterPreviousStrategy strategy,
            int recentCount,
            int fixedCount,
            int exposureRatio
    ) {
        NewsletterPreviousPolicy policy = TestFixture.createNewsletterPreviousPolicy(
                newsletter.getId(),
                strategy,
                recentCount,
                fixedCount,
                exposureRatio
        );
        newsletterPreviousPolicyRepository.save(policy);
    }

    private String uniqueEmail(String prefix) {
        return uniqueValue(prefix) + "@bombom.news";
    }

    private String uniqueValue(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
