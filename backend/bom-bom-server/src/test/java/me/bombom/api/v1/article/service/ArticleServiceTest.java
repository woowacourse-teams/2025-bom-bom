package me.bombom.api.v1.article.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.dto.request.ArticlesOptionsRequest;
import me.bombom.api.v1.article.dto.response.ArticleDetailResponse;
import me.bombom.api.v1.article.dto.response.ArticleNewsletterStatisticsResponse;
import me.bombom.api.v1.article.dto.response.ArticleResponse;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.enums.Gender;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterDetailRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.test.util.ReflectionTestUtils;

@IntegrationTest
class ArticleServiceTest {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2025, 7, 15, 10, 0);

    @Autowired
    private ArticleService articleService;

    @Autowired
    private NewsletterRepository newsletterRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MemberRepository memberRepository;

    List<Category> categories;
    List<Newsletter> newsletters;
    List<Article> articles;
    Member member;
    @Autowired
    private NewsletterDetailRepository newsletterDetailRepository;

    @BeforeEach
    public void setup() {
        newsletterRepository.deleteAllInBatch();
        articleRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        member = TestFixture.normalMemberFixture();
        memberRepository.save(member);
        categories = TestFixture.createCategories();
        categoryRepository.saveAll(categories);
        List<NewsletterDetail> newsletterDetails = TestFixture.createNewsletterDetails();
        newsletterDetailRepository.saveAll(newsletterDetails);
        newsletters = TestFixture.createNewslettersWithDetails(categories, newsletterDetails);
        newsletterRepository.saveAll(newsletters);
        articles = TestFixture.createArticles(member, newsletters);
        articleRepository.saveAll(articles);
    }

    @Test
    void 아티클_목록_조회_DESC_정렬_테스트() {
        // given
        Pageable pageable = PageRequest.of(0, 10); // 충분한 크기로 전체 조회

        // when
        Page<ArticleResponse> result = articleService.getArticles(
                member,
                ArticlesOptionsRequest.of(null, null, null),
                pageable
        );

        // then
        List<ArticleResponse> content = result.getContent();
        assertSoftly(softly -> {
            softly.assertThat(result.getTotalElements()).isEqualTo(articles.size());
            softly.assertThat(content).hasSize(articles.size());
            softly.assertThat(content.get(0).arrivedDateTime()).isAfter(content.get(1).arrivedDateTime());
            softly.assertThat(content.get(1).arrivedDateTime()).isAfter(content.get(2).arrivedDateTime());
        });
    }

    @Test
    void 아티클_목록_조회_ASC_정렬_테스트() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Direction.ASC, "arrivedDateTime"));

        // when
        Page<ArticleResponse> result = articleService.getArticles(
                member,
                ArticlesOptionsRequest.of(null, null, null),
                pageable
        );

        // then
        List<ArticleResponse> content = result.getContent();
        assertSoftly(softly -> {
            softly.assertThat(result.getTotalElements()).isEqualTo(articles.size());
            softly.assertThat(content).hasSize(articles.size());
            softly.assertThat(content.get(0).arrivedDateTime()).isBefore(content.get(1).arrivedDateTime());
            softly.assertThat(content.get(1).arrivedDateTime()).isBefore(content.get(2).arrivedDateTime());
        });
    }

    @Test
    void 아티클_목록_조회_뉴스레터_필터링_테스트() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Newsletter newsletter = newsletters.getFirst();
        Long newsletterId = newsletter.getId();

        // when
        Page<ArticleResponse> result = articleService.getArticles(
                member,
                ArticlesOptionsRequest.of(null, newsletterId, null),
                pageable
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.getTotalElements()).isEqualTo(1);
            softly.assertThat(result.getContent()).hasSize(1);
            softly.assertThat(result.getContent()).extracting("newsletter")
                    .extracting("name")
                    .containsExactly(newsletter.getName());
        });
    }

    @Test
    void 아티클_목록_조회_날짜_필터링_테스트() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<ArticleResponse> result = articleService.getArticles(
                member,
                ArticlesOptionsRequest.of(BASE_TIME.toLocalDate(), null, null),
                pageable
        );

        // then - 하루 전 아티클 제외하고 3개
        assertSoftly(softly -> {
            softly.assertThat(result.getTotalElements()).isEqualTo(articles.size() - 1);
            softly.assertThat(result.getContent()).hasSize(articles.size() - 1);
        });
    }

    @Test
    void 아티클_목록_조회_제목_검색_테스트() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<ArticleResponse> result = articleService.getArticles(
                member,
                ArticlesOptionsRequest.of(BASE_TIME.toLocalDate(), null, "뉴스"),
                pageable
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.getContent())
                    .extracting(ArticleResponse::title)
                    .allMatch(title -> title.contains("뉴스"));
            softly.assertThat(result.getContent()).hasSize(2);
        });
    }

    @Test
    void 아티클_목록_조회_뉴스레터가_존재하지_않으면_예외() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when & then
        assertThatThrownBy(() -> articleService.getArticles(
                member,
                ArticlesOptionsRequest.of(null, 0L, null),
                pageable
        )).isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 아티클_목록_조회_정렬_기준_필드가_존재하지_않으면_예외() {
        // given
        Pageable pageable = PageRequest.of(
                0,
                10,
                Sort.by(new Order(Direction.DESC, "invalidField"))
        );

        // when & then
        assertThatThrownBy(() -> articleService.getArticles(
                member,
                ArticlesOptionsRequest.of(null, null, null),
                pageable
        )).isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.INVALID_REQUEST_PARAMETER_VALIDATION);
    }

    @Test
    void 아티클_목록_조회_페이징_첫번째_페이지_테스트() {
        // given
        Pageable firstPage = PageRequest.of(0, 2);

        // when
        Page<ArticleResponse> result = articleService.getArticles(
                member,
                ArticlesOptionsRequest.of(null, null, null),
                firstPage
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.getTotalElements()).isEqualTo(4);
            softly.assertThat(result.getTotalPages()).isEqualTo(2);
            softly.assertThat(result.getContent()).hasSize(2);
            softly.assertThat(result.getNumber()).isEqualTo(0);
            softly.assertThat(result.getSize()).isEqualTo(2);
            softly.assertThat(result.isFirst()).isTrue();
            softly.assertThat(result.isLast()).isFalse();
            softly.assertThat(result.hasNext()).isTrue();
            softly.assertThat(result.hasPrevious()).isFalse();
        });
    }

    @Test
    void 아티클_목록_조회_페이징_두번째_페이지_테스트() {
        // given
        Pageable secondPage = PageRequest.of(1, 2);

        // when
        Page<ArticleResponse> result = articleService.getArticles(
                member,
                ArticlesOptionsRequest.of(null, null, null),
                secondPage
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.getTotalElements()).isEqualTo(4);
            softly.assertThat(result.getTotalPages()).isEqualTo(2);
            softly.assertThat(result.getContent()).hasSize(2);
            softly.assertThat(result.getNumber()).isEqualTo(1);
            softly.assertThat(result.getSize()).isEqualTo(2);
            softly.assertThat(result.isFirst()).isFalse();
            softly.assertThat(result.isLast()).isTrue();
            softly.assertThat(result.hasNext()).isFalse();
            softly.assertThat(result.hasPrevious()).isTrue();
        });
    }

    @Test
    void 아티클_목록_조회_페이징_DESC_정렬_테스트() {
        // given
        Pageable pageable = PageRequest.of(0, 2);

        // when
        Page<ArticleResponse> result = articleService.getArticles(
                member,
                ArticlesOptionsRequest.of(null, null, null),
                pageable
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.getTotalElements()).isEqualTo(4);
            softly.assertThat(result.getContent()).hasSize(2);
            softly.assertThat(result.getTotalPages()).isEqualTo(2);
            softly.assertThat(result.getContent().get(0).arrivedDateTime())
                    .isAfter(result.getContent().get(1).arrivedDateTime());
        });
    }

    @Test
    void 아티클_목록_조회_페이징_ASC_정렬_테스트() {
        // given
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Direction.ASC, "arrivedDateTime"));

        // when
        Page<ArticleResponse> result = articleService.getArticles(
                member,
                ArticlesOptionsRequest.of(null, null, null),
                pageable
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.getTotalElements()).isEqualTo(4);
            softly.assertThat(result.getContent()).hasSize(2);
            softly.assertThat(result.getTotalPages()).isEqualTo(2);
            softly.assertThat(result.getContent().get(0).arrivedDateTime())
                    .isBefore(result.getContent().get(1).arrivedDateTime());
        });
    }

    @Test
    void 아티클_상세_조회_성공_테스트() {
        // given
        Article article = articles.getFirst();
        Newsletter newsletter = newsletters.getFirst();

        // when
        ArticleDetailResponse result = articleService.getArticleDetail(article.getId(), member);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.title()).isEqualTo(article.getTitle());
            softly.assertThat(result.newsletter().name()).isEqualTo(newsletter.getName());
        });
    }

    @Test
    void 아티클_상세_조회_아티클이_존재하지_않으면_예외() {
        // when & then
        assertThatThrownBy(() -> articleService.getArticleDetail(0L, member))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 아티클_상세_조회_뉴스레터가_존재하지_않으면_예외() {
        // given
        Article article = Article.builder()
                .title("테스트 아티클")
                .contents("<p>테스트 내용</p>")
                .thumbnailUrl("https://example.com/test.png")
                .expectedReadTime(3)
                .contentsSummary("테스트 요약")
                .isRead(false)
                .memberId(member.getId())
                .newsletterId(0L) // 존재하지 않는 뉴스레터 ID
                .arrivedDateTime(BASE_TIME)
                .build();
        articleRepository.save(article);

        // when & then
        assertThatThrownBy(() -> articleService.getArticleDetail(article.getId(), member))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 아티클_상세_조회_카테고리가_존재하지_않으면_예외() {
        // given
        NewsletterDetail newsletterDetail = TestFixture.createNewsletterDetail(false);
        newsletterDetailRepository.save(newsletterDetail);
        Newsletter newsletter = TestFixture.createNewsletter("테스트 뉴스레터", "test@example.com", 0L, newsletterDetail.getId());
        newsletterRepository.save(newsletter);
        Article article = TestFixture.createArticle("제목", member.getId(), newsletter.getId(), BASE_TIME);
        articleRepository.save(article);

        // when & then
        assertThatThrownBy(() -> articleService.getArticleDetail(article.getId(), member))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 아티클_상세_조회_멤버가_이메일의_주인이_아니면_예외() {
        //given
        Member member2 = Member.builder()
                .provider("provider2")
                .providerId("providerId2")
                .email("email2")
                .nickname("nickname2")
                .gender(Gender.FEMALE)
                .roleId(1L)
                .build();
        memberRepository.save(member2);

        // when & then
        assertThatThrownBy(() -> articleService.getArticleDetail(articles.getFirst().getId(), member2))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.FORBIDDEN_RESOURCE);
    }

    @Test
    void 다_읽음_갱신_아티클이_존재하지_않으면_예외() {
        // when & then
        assertThatThrownBy(() -> articleService.markAsRead(0L, member))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 다_읽음_갱신_아티클_주인이_일치하지_않으면_예외() {
        // given
        Member otherMember = Member.builder()
                .provider("provider2")
                .providerId("providerId2")
                .email("email2")
                .nickname("nickname2")
                .gender(Gender.FEMALE)
                .roleId(1L)
                .build();
        memberRepository.save(otherMember);

        Article article = TestFixture.createArticle(
                "제목",
                member.getId(),
                newsletters.getFirst().getId(),
                BASE_TIME
        );
        articleRepository.save(article);

        // when & then
        assertThatThrownBy(() -> articleService.markAsRead(article.getId(), otherMember))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.FORBIDDEN_RESOURCE);
    }

    @Test
    void 제목_필터링_된_뉴스레터_별_아티클_개수를_조회한다() {
        // when
        String keyword = "AI";

        List<Article> testArticles = List.of(
                TestFixture.createArticle("AI와 디자인", member.getId(), newsletters.get(0).getId(), BASE_TIME),
                TestFixture.createArticle("생성형 AI 추천", member.getId(), newsletters.get(1).getId(), BASE_TIME),
                TestFixture.createArticle("리빙 인테리어", member.getId(), newsletters.get(2).getId(), BASE_TIME),
                TestFixture.createArticle("북카페 추천", member.getId(), newsletters.get(0).getId(), BASE_TIME),
                TestFixture.createArticle("직업과 AI의 상관관계", member.getId(), newsletters.get(1).getId(), BASE_TIME)
        );
        articleRepository.saveAll(testArticles);

        ArticleNewsletterStatisticsResponse result = articleService.getArticleNewsletterStatistics(
                member,
                keyword
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.totalCount()).isEqualTo(3);
            softly.assertThat(result.newsletters().get(0).name()).isEqualTo("IT타임즈");
            softly.assertThat(result.newsletters().get(0).articleCount()).isEqualTo(2);
            softly.assertThat(result.newsletters().get(1).name()).isEqualTo("뉴스픽");
            softly.assertThat(result.newsletters().get(1).articleCount()).isEqualTo(1);
        });
    }

    @Test
    void 전체_뉴스레터_별_아티클_개수를_조회한다() {
        // when
        ArticleNewsletterStatisticsResponse result = articleService.getArticleNewsletterStatistics(
                member,
                null
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.totalCount()).isEqualTo(4);
            softly.assertThat(result.newsletters().get(0).name()).isEqualTo("비즈레터");
            softly.assertThat(result.newsletters().get(0).articleCount()).isEqualTo(2);
            softly.assertThat(result.newsletters().get(1).name()).isEqualTo("뉴스픽");
            softly.assertThat(result.newsletters().get(1).articleCount()).isEqualTo(1);
            softly.assertThat(result.newsletters().get(2).name()).isEqualTo("IT타임즈");
            softly.assertThat(result.newsletters().get(2).articleCount()).isEqualTo(1);
        });
    }

    @Test
    void 키워드가_빈_문자열인_경우_전체_조회한다() {
        // when
        ArticleNewsletterStatisticsResponse result = articleService.getArticleNewsletterStatistics(
                member,
                ""
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.totalCount()).isEqualTo(4);
            softly.assertThat(result.newsletters()).hasSize(3); // 모든 뉴스레터
        });
    }

    @Test
    void 키워드가_공백만_있는_경우_전체_조회한다() {
        // when
        ArticleNewsletterStatisticsResponse result = articleService.getArticleNewsletterStatistics(
                member,
                "   "
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.totalCount()).isEqualTo(4);
            softly.assertThat(result.newsletters()).hasSize(3); // 모든 뉴스레터
        });
    }

    @Test
    void 키워드_앞뒤_공백이_제거되어_검색된다() {
        // given
        List<Article> testArticles = List.of(
                TestFixture.createArticle("AI 기술", member.getId(), newsletters.get(0).getId(), BASE_TIME),
                TestFixture.createArticle("머신러닝", member.getId(), newsletters.get(1).getId(), BASE_TIME)
        );
        articleRepository.saveAll(testArticles);

        // when
        ArticleNewsletterStatisticsResponse result = articleService.getArticleNewsletterStatistics(
                member,
                "  AI  "  // 앞뒤 공백
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.totalCount()).isEqualTo(1);
            softly.assertThat(result.newsletters().get(0).articleCount()).isEqualTo(1);
        });
    }

    @Test
    void 키워드에_일치하는_아티클이_없는_경우() {
        // when
        ArticleNewsletterStatisticsResponse result = articleService.getArticleNewsletterStatistics(
                member,
                "존재하지않는키워드"
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.totalCount()).isEqualTo(0);
            softly.assertThat(result.newsletters()).isEmpty();
        });
    }

//    @Test
//    void 대소문자_구분없이_키워드_검색이_된다() {
//        // given
//        List<Article> testArticles = List.of(
//                TestFixture.createArticle("AI Technology", member.getId(), newsletters.get(0).getId(), BASE_TIME),
//                TestFixture.createArticle("ai development", member.getId(), newsletters.get(1).getId(), BASE_TIME)
//        );
//        articleRepository.saveAll(testArticles);
//
//        // when
//        ArticleNewsletterStatisticsResponse result = articleService.getArticleNewsletterStatistics(
//                member,
//                "ai"
//        );
//
//        // then
//        assertSoftly(softly -> {
//            softly.assertThat(result.totalCount()).isEqualTo(2);
//            softly.assertThat(result.newsletters()).hasSize(2);
//        });
//    }

    @Test
    void 부분_문자열로_키워드_검색이_된다() {
        // given
        List<Article> testArticles = List.of(
                TestFixture.createArticle("프로그래밍 언어", member.getId(), newsletters.get(0).getId(), BASE_TIME),
                TestFixture.createArticle("그래픽 디자인", member.getId(), newsletters.get(1).getId(), BASE_TIME),
                TestFixture.createArticle("데이터베이스", member.getId(), newsletters.get(2).getId(), BASE_TIME)
        );
        articleRepository.saveAll(testArticles);

        // when
        ArticleNewsletterStatisticsResponse result = articleService.getArticleNewsletterStatistics(
                member,
                "그래"  // "프로그래밍", "그래픽" 모두 매치
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.totalCount()).isEqualTo(2);
            softly.assertThat(result.newsletters()).hasSize(2);
        });
    }

    @Test
    void 특정_뉴스레터에만_키워드가_매치되는_경우() {
        // given
        List<Article> testArticles = List.of(
                TestFixture.createArticle("특별한 이벤트", member.getId(), newsletters.get(0).getId(), BASE_TIME),
                TestFixture.createArticle("일반적인 뉴스", member.getId(), newsletters.get(1).getId(), BASE_TIME),
                TestFixture.createArticle("또 다른 뉴스", member.getId(), newsletters.get(2).getId(), BASE_TIME)
        );
        articleRepository.saveAll(testArticles);

        // when
        ArticleNewsletterStatisticsResponse result = articleService.getArticleNewsletterStatistics(
                member,
                "특별한"
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.totalCount()).isEqualTo(1);
            softly.assertThat(result.newsletters()).hasSize(1);
            softly.assertThat(result.newsletters().get(0).name()).isEqualTo("뉴스픽"); // newsletters.get(0)에 해당
            softly.assertThat(result.newsletters().get(0).articleCount()).isEqualTo(1);
        });
    }

    @Test
    void 관리자_아티클_정리_테스트() {
        // given
        Member admin = Member.builder()
                .provider("admin")
                .providerId("admin123")
                .email("admin@bombom.news")
                .nickname("봄봄")
                .gender(Gender.MALE)
                .roleId(1L)
                .build();
        memberRepository.save(admin);
        
        // 테스트용 설정값 오버라이드
        ReflectionTestUtils.setField(articleService, "PREVIOUS_ARTICLE_ADMIN_ID", admin.getId());

        Newsletter newsletter1 = newsletters.get(0);
        
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

        // when - 서비스를 통해 호출 (트랜잭션 처리)
        int deletedCount = articleService.cleanupOldPreviousArticles();

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
            
            // 일반 사용자 아티클은 영향받지 않았는지 확인
            long memberCount = articleRepository.findAll().stream()
                .filter(article -> article.getMemberId().equals(member.getId()))
                .count();
            softly.assertThat(memberCount).isEqualTo(4);
        });
    }
}
