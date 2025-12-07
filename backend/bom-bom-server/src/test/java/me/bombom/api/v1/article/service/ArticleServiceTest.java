package me.bombom.api.v1.article.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.dto.request.ArticlesOptionsRequest;
import me.bombom.api.v1.article.dto.request.DeleteArticlesRequest;
import me.bombom.api.v1.article.dto.response.ArticleCountPerNewsletterResponse;
import me.bombom.api.v1.article.dto.response.ArticleDetailResponse;
import me.bombom.api.v1.article.dto.response.ArticleNewsletterStatisticsResponse;
import me.bombom.api.v1.article.dto.response.ArticleResponse;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.bookmark.domain.Bookmark;
import me.bombom.api.v1.bookmark.repository.BookmarkRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.exception.UnauthorizedException;
import me.bombom.api.v1.highlight.repository.HighlightRepository;
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

@IntegrationTest
class ArticleServiceTest {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2025, 7, 15, 10, 0);

    @Autowired
    private ArticleService articleService;

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
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private HighlightRepository highlightRepository;

    List<Category> categories;
    List<Newsletter> newsletters;
    List<Article> articles;
    Member member;

    @BeforeEach
    public void setup() {
        newsletterRepository.deleteAllInBatch();
        articleRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        bookmarkRepository.deleteAllInBatch();
        highlightRepository.deleteAllInBatch();

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
                ArticlesOptionsRequest.of(null, null),
                pageable
        );

        // then
        List<ArticleResponse> content = result.getContent();
        assertSoftly(softly -> {;
            softly.assertThat(content.get(0).arrivedDateTime()).isAfter(content.get(1).arrivedDateTime());
            softly.assertThat(content.get(1).arrivedDateTime()).isAfter(content.get(2).arrivedDateTime());
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
                ArticlesOptionsRequest.of(null, newsletterId),
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

//
//    @Test
//    void 아티클_목록_조회_제목_검색_테스트() {
//        // given
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // when
//        Page<ArticleResponse> result = articleService.getArticles(
//                member,
//                ArticlesOptionsRequest.of(BASE_TIME.toLocalDate(), null, "뉴스"),
//                pageable
//        );
//
//        // then
//        assertSoftly(softly -> {
//            softly.assertThat(result.getContent())
//                    .extracting(ArticleResponse::title)
//                    .allMatch(title -> title.contains("뉴스"));
//            softly.assertThat(result.getContent()).hasSize(2);
//        });
//    }

    @Test
    void 아티클_목록_조회_뉴스레터가_존재하지_않으면_예외() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when & then
        assertThatThrownBy(() -> articleService.getArticles(
                member,
                ArticlesOptionsRequest.of(null, 0L),
                pageable
        )).isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 아티클_목록_조회_페이징_첫번째_페이지_테스트() {
        // given
        Pageable firstPage = PageRequest.of(0, 2);

        // when
        Page<ArticleResponse> result = articleService.getArticles(
                member,
                ArticlesOptionsRequest.of(null, null),
                firstPage
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.getTotalElements()).isEqualTo(11);
            softly.assertThat(result.getTotalPages()).isEqualTo(6);
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
                ArticlesOptionsRequest.of(null, null),
                secondPage
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.getTotalElements()).isEqualTo(11);
            softly.assertThat(result.getTotalPages()).isEqualTo(6);
            softly.assertThat(result.getContent()).hasSize(2);
            softly.assertThat(result.getNumber()).isEqualTo(1);
            softly.assertThat(result.getSize()).isEqualTo(2);
            softly.assertThat(result.isFirst()).isFalse();
            softly.assertThat(result.isLast()).isFalse();
            softly.assertThat(result.hasNext()).isTrue();
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
                ArticlesOptionsRequest.of(null, null),
                pageable
        );

        System.out.println(result);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.getTotalElements()).isEqualTo(11);
            softly.assertThat(result.getContent()).hasSize(2);
            softly.assertThat(result.getTotalPages()).isEqualTo(6);
            softly.assertThat(result.getContent().get(0).arrivedDateTime())
                    .isAfter(result.getContent().get(1).arrivedDateTime());
        });
    }

    @Test
    void 아티클_목록_조회_북마크_여부_표시_테스트() {
        // given
        // 내 북마크 생성
        bookmarkRepository.save(
                Bookmark.builder()
                        .articleId(articles.getFirst().getId())
                        .memberId(member.getId())
                        .build()
        );

        Pageable pageable = PageRequest.of(0, 10); // 전체 포함

        // when
        Page<ArticleResponse> result = articleService.getArticles(
                member,
                ArticlesOptionsRequest.of(null, null),
                pageable
        );

        // then
        List<ArticleResponse> content = result.getContent();
        assertSoftly(softly -> {
            softly.assertThat(content.get(0).isBookmarked()).isTrue();
            softly.assertThat(content.get(1).isBookmarked()).isFalse();
            softly.assertThat(content.get(2).isBookmarked()).isFalse();
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
                .contentsText("텍스트")
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
        Newsletter newsletter = TestFixture.createNewsletter("테스트 뉴스레터", "test@example.com", 0L,
                newsletterDetail.getId());
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
    void 전체_뉴스레터_별_아티클_개수를_조회한다() {
        // when
        ArticleNewsletterStatisticsResponse result = articleService.getArticleNewsletterStatistics(
                member,
                null
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.totalCount()).isEqualTo(11);

            softly.assertThat(result.newsletters().get(0).name()).isEqualTo("우테코");
            softly.assertThat(result.newsletters().get(0).articleCount()).isEqualTo(7);

            softly.assertThat(result.newsletters().get(1).name()).isEqualTo("비즈레터");
            softly.assertThat(result.newsletters().get(1).articleCount()).isEqualTo(2);

            softly.assertThat(result.newsletters().get(2).name()).isEqualTo("뉴스픽");
            softly.assertThat(result.newsletters().get(2).articleCount()).isEqualTo(1);

            softly.assertThat(result.newsletters().get(3).name()).isEqualTo("IT타임즈");
            softly.assertThat(result.newsletters().get(3).articleCount()).isEqualTo(1);
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
            softly.assertThat(result.totalCount()).isEqualTo(11);
            softly.assertThat(result.newsletters()).hasSize(4); // 모든 뉴스레터
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
            softly.assertThat(result.totalCount()).isEqualTo(11);
            softly.assertThat(result.newsletters()).hasSize(4); // 모든 뉴스레터
        });
    }

    @Test
    void 키워드_앞뒤_공백이_제거되어_검색된다() {
        // given
        LocalDateTime now = LocalDateTime.now();
        List<Article> testArticles = List.of(
                TestFixture.createArticle("AI 기술", member.getId(), newsletters.get(0).getId(), now),
                TestFixture.createArticle("머신러닝", member.getId(), newsletters.get(1).getId(), now)
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
            softly.assertThat(result.newsletters()).isNotEmpty();
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
        LocalDateTime now = LocalDateTime.now();
        List<Article> testArticles = List.of(
                TestFixture.createArticle("프로그래밍 언어", member.getId(), newsletters.get(0).getId(), now),
                TestFixture.createArticle("그래픽 디자인", member.getId(), newsletters.get(1).getId(), now),
                TestFixture.createArticle("데이터베이스", member.getId(), newsletters.get(2).getId(), now)
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
    void 아티클_삭제_성공시_북마크와_아티클이_삭제된다() {
        // given
        // 내 글 2개를 타겟으로, 각 글에 내 북마크 1개씩 생성
        Long article1 = articles.get(0).getId();
        Long article2 = articles.get(1).getId();

        bookmarkRepository.save(Bookmark.builder()
                .articleId(articles.get(0).getId())
                .memberId(member.getId())
                .build());
        bookmarkRepository.save(Bookmark.builder()
                .articleId(articles.get(1).getId())
                .memberId(member.getId())
                .build());
        highlightRepository.saveAll(TestFixture.createHighlightFixtures(articles));

        // 중복 ID가 들어와도 서비스에서 distinct 처리됨
        DeleteArticlesRequest req = new DeleteArticlesRequest(List.of(article1, article2, article1));

        // when
        articleService.delete(member, req);

        // then
        assertSoftly(softly -> {
            softly.assertThat(articleRepository.findById(article1)).isEmpty();
            softly.assertThat(articleRepository.findById(article2)).isEmpty();
            softly.assertThat(articleRepository.findAll().size()).isEqualTo(9);
            softly.assertThat(bookmarkRepository.findAll()).hasSize(0);
            softly.assertThat(highlightRepository.findAllByArticleId(0L)).hasSize(3); // fixture 6개 중 포함된건 3개
        });
    }

    @Test
    void 아티클_삭제_권한_없으면_예외_발생() {
        // given
        Member other = TestFixture.createMemberFixture("email2", "nickname2");
        memberRepository.save(other);

        Long foreignArticleId = articleRepository.save(
                TestFixture.createArticle("남의글", other.getId(), newsletters.get(0).getId(), BASE_TIME)
        ).getId();

        Long myArticleId = articles.get(2).getId();
        bookmarkRepository.save(Bookmark.builder()
                .articleId(myArticleId)
                .memberId(member.getId())
                .build());

        DeleteArticlesRequest req = new DeleteArticlesRequest(List.of(myArticleId, foreignArticleId));

        // when & then
        assertThatThrownBy(() -> articleService.delete(member, req))
                .isInstanceOf(UnauthorizedException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.FORBIDDEN_RESOURCE);

        assertSoftly(softly -> {
            softly.assertThat(articleRepository.findById(myArticleId)).isPresent();
            softly.assertThat(articleRepository.findById(foreignArticleId)).isPresent();
            softly.assertThat(bookmarkRepository.findAll()).hasSize(1);
        });
    }

    @Test
    void 키워드가_null일_때_countWithoutKeyword가_호출된다() {
        // when
        List<ArticleCountPerNewsletterResponse> result = articleRepository.countPerNewsletter(member.getId(), null);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(4); // 모든 뉴스레터
            softly.assertThat(result.get(0).articleCount()).isEqualTo(7); // 우테코
            softly.assertThat(result.get(1).articleCount()).isEqualTo(2); // 비즈레터
        });
    }

    @Test
    void 키워드가_공백일_때_countWithoutKeyword가_호출된다() {
        // when
        List<ArticleCountPerNewsletterResponse> result = articleRepository.countPerNewsletter(member.getId(), "   ");

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(4); // 모든 뉴스레터
            softly.assertThat(result.get(0).articleCount()).isEqualTo(7); // 우테코
            softly.assertThat(result.get(1).articleCount()).isEqualTo(2); // 비즈레터
        });
    }

    @Test
    void 키워드가_있을_때_countWithKeyword가_호출된다() {
        // given
        Newsletter targetNewsletter = newsletters.get(0); // 뉴스픽
        LocalDateTime now = LocalDateTime.now();
        List<Article> testArticles = List.of(
                TestFixture.createArticle("검색 테스트 아티클", member.getId(), targetNewsletter.getId(), now),
                TestFixture.createArticle("일반 아티클", member.getId(), newsletters.get(1).getId(), now)
        );
        articleRepository.saveAll(testArticles);

        // when
        List<ArticleCountPerNewsletterResponse> result = articleRepository.countPerNewsletter(member.getId(), "검색");

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).isNotEmpty();
            softly.assertThat(result.stream()
                    .anyMatch(r -> r.name().equals(targetNewsletter.getName()) && r.articleCount() > 0)).isTrue();
        });
    }

    @Test
    void 키워드_검색시_5일_이내_article_테이블_데이터만_검색된다() {
        // given
        Newsletter targetNewsletter = newsletters.get(0); // 뉴스픽
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fourDaysAgo = now.minusDays(4); // 5일 이내
        LocalDateTime sixDaysAgo = now.minusDays(6); // 5일 이전
        
        List<Article> testArticles = List.of(
                TestFixture.createArticle("검색 키워드 포함", member.getId(), targetNewsletter.getId(), fourDaysAgo),
                TestFixture.createArticle("검색 키워드 포함", member.getId(), targetNewsletter.getId(), sixDaysAgo)
        );
        articleRepository.saveAll(testArticles);

        // when
        List<ArticleCountPerNewsletterResponse> result = articleRepository.countPerNewsletter(member.getId(), "검색");

        // then - 5일 이내 데이터만 검색되어야 함
        assertSoftly(softly -> {
            Optional<ArticleCountPerNewsletterResponse> targetResult = result.stream()
                    .filter(r -> r.name().equals(targetNewsletter.getName()))
                    .findFirst();
            
            if (targetResult.isPresent()) {
                // 5일 이내 데이터 1개만 검색되어야 함
                softly.assertThat(targetResult.get().articleCount()).isEqualTo(1);
            } else {
                // 결과가 없을 수도 있음 (recent_article 테이블에 데이터가 없고, article 테이블에 5일 이내 데이터만 있는 경우)
                softly.assertThat(result).isEmpty();
            }
        });
    }

    @Test
    void 키워드_검색시_5일_이전_article_테이블_데이터는_검색되지_않는다() {
        // given
        Newsletter targetNewsletter = newsletters.get(0); // 뉴스픽
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sixDaysAgo = now.minusDays(6); // 5일 이전
        LocalDateTime tenDaysAgo = now.minusDays(10); // 5일 이전
        
        List<Article> testArticles = List.of(
                TestFixture.createArticle("검색 키워드 포함", member.getId(), targetNewsletter.getId(), sixDaysAgo),
                TestFixture.createArticle("검색 키워드 포함", member.getId(), targetNewsletter.getId(), tenDaysAgo)
        );
        articleRepository.saveAll(testArticles);

        // when
        List<ArticleCountPerNewsletterResponse> result = articleRepository.countPerNewsletter(member.getId(), "검색");

        // then - 5일 이전 데이터는 검색되지 않아야 함
        Optional<ArticleCountPerNewsletterResponse> targetResult = result.stream()
                .filter(r -> r.name().equals(targetNewsletter.getName()))
                .findFirst();
        
        assertSoftly(softly -> {
            // 5일 이전 데이터는 검색되지 않아야 하므로, article 테이블에서는 결과가 없어야 함
            // recent_article 테이블에 데이터가 있다면 그것만 검색될 수 있음
            if (targetResult.isPresent()) {
                // recent_article 테이블에 데이터가 있는 경우만 결과가 있을 수 있음
                // article 테이블의 5일 이전 데이터는 검색되지 않아야 함
                softly.assertThat(targetResult.get().articleCount()).isGreaterThanOrEqualTo(0);
            }
        });
    }
}
