package me.bombom.api.v1.article.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.dto.ArticleDetailResponse;
import me.bombom.api.v1.article.dto.ArticleResponse;
import me.bombom.api.v1.article.enums.SortOption;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
@Import(ArticleService.class)
class ArticleServiceTest {

    private static final LocalDateTime baseTime = LocalDateTime.of(2025, 7, 15, 10, 0);

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

    @BeforeEach
    public void setup() {
        member = TestFixture.normalMemberFixture();
        memberRepository.save(member);
        categories = List.of(
                Category.builder()
                        .name("경제")
                        .build(),
                Category.builder()
                        .name("테크")
                        .build(),
                Category.builder()
                        .name("푸드")
                        .build()
        );
        categoryRepository.saveAll(categories);
        newsletters = List.of(
                Newsletter.builder()
                        .name("뉴스픽")
                        .description("뉴스픽 요약 뉴스")
                        .imageUrl("https://cdn.bombom.me/img1.png")
                        .email("news@newspick.com")
                        .categoryId(categories.get(0).getId())
                        .detailId(1L)
                        .build(),
                Newsletter.builder()
                        .name("IT타임즈")
                        .description("IT 업계 트렌드")
                        .imageUrl("https://cdn.bombom.me/img2.png")
                        .email("editor@ittimes.io")
                        .categoryId(categories.get(1).getId())
                        .detailId(2L)
                        .build(),
                Newsletter.builder()
                        .name("비즈레터")
                        .description("비즈니스 뉴스 큐레이션")
                        .imageUrl("https://cdn.bombom.me/img3.png")
                        .email("biz@biz.com")
                        .categoryId(categories.get(2).getId())
                        .detailId(3L)
                        .build()
        );
        newsletterRepository.saveAll(newsletters);
        articles = List.of(
                Article.builder()
                        .title("개발자 생산성을 높이는 도구들")
                        .contents("<h1>개발자 생산성을 높이는 도구들</h1>")
                        .thumbnailUrl("https://example.com/images/dev-tools.png")
                        .expectedReadTime(5)
                        .contentsSummary("생산성을 높이는 다양한 개발 도구들을 소개합니다.")
                        .isRead(false)
                        .memberId(member.getId())
                        .newsletterId(newsletters.get(0).getId())
                        .arrivedDateTime(baseTime.minusMinutes(5))
                        .build(),
                Article.builder()
                        .title("AI가 바꾸는 일상의 풍경")
                        .contents("<h1>AI가 바꾸는 일상의 풍경</h1>")
                        .thumbnailUrl("https://example.com/images/ai-life.png")
                        .expectedReadTime(7)
                        .contentsSummary("AI 기술이 우리의 일상생활에 어떤 변화를 가져왔는지 정리했습니다.")
                        .isRead(true)
                        .memberId(member.getId())
                        .newsletterId(newsletters.get(1).getId())
                        .arrivedDateTime(baseTime.minusMinutes(10))
                        .build(),
                Article.builder()
                        .title("2025년 IT 트렌드 미리보기")
                        .contents("<h1>2025년 IT 트렌드 미리보기</h1>")
                        .thumbnailUrl("https://example.com/images/it-trend.png")
                        .expectedReadTime(4)
                        .contentsSummary("다가오는 IT 트렌드를 전망하고 주요 기술을 짚어봅니다.")
                        .isRead(false)
                        .memberId(member.getId())
                        .newsletterId(newsletters.get(2).getId())
                        .arrivedDateTime(baseTime.minusMinutes(20))
                        .build(),
                Article.builder() // 하루 전 아티클
                        .title("2025년 패션 트렌드 미리보기")
                        .contents("<h1>2025년 패션 트렌드 미리보기</h1>")
                        .thumbnailUrl("https://example.com/images/it-trend.png")
                        .expectedReadTime(8)
                        .contentsSummary("다가오는 패션 트렌드를 전망하고 주요 기술을 짚어봅니다.")
                        .isRead(false)
                        .memberId(member.getId())
                        .newsletterId(newsletters.get(2).getId())
                        .arrivedDateTime(baseTime.minusDays(1))
                        .build()
        );
        articleRepository.saveAll(articles);
    }

    @Test
    void 아티클_목록_조회_DESC_정렬_테스트() {
        // given
        Pageable pageable = PageRequest.of(0, 10); // 충분한 크기로 전체 조회
        
        // when
        Page<ArticleResponse> result = articleService.getArticles(
                member.getId(),
                null,
                null,
                SortOption.DESC,
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
        Pageable pageable = PageRequest.of(0, 10);
        
        // when
        Page<ArticleResponse> result = articleService.getArticles(
                member.getId(),
                null,
                null,
                SortOption.ASC,
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
    void 아티클_목록_조회_카테고리_필터링_테스트() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        String category = categories.getFirst().getName();

        // when
        Page<ArticleResponse> result = articleService.getArticles(
                member.getId(),
                null,
                category,
                SortOption.DESC,
                pageable
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.getTotalElements()).isEqualTo(1);
            softly.assertThat(result.getContent()).hasSize(1);
            softly.assertThat(result.getContent()).extracting("newsletter")
                    .extracting("category")
                    .containsExactly(category);
        });
    }

    @Test
    void 아티클_목록_조회_날짜_필터링_테스트() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        
        // when
        Page<ArticleResponse> result = articleService.getArticles(
                member.getId(),
                baseTime.toLocalDate(),
                null,
                SortOption.DESC,
                pageable
        );

        // then - 하루 전 아티클 제외하고 3개
        assertSoftly(softly -> {
            softly.assertThat(result.getTotalElements()).isEqualTo(articles.size() - 1);
            softly.assertThat(result.getContent()).hasSize(articles.size() - 1);
        });
    }

    @Test
    void 아티클_목록_조회_멤버가_존재하지_않으면_예외() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when & then
        assertThatThrownBy(() -> articleService.getArticles(
                0L,
                null,
                categories.getFirst().getName(),
                SortOption.DESC,
                pageable
        )).isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 아티클_목록_조회_카테고리가_존재하지_않으면_예외() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        
        // when & then
        assertThatThrownBy(() -> articleService.getArticles(
                member.getId(),
                null,
                "Invalid Category",
                SortOption.DESC,
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
                member.getId(),
                null,
                null,
                SortOption.DESC,
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
                member.getId(),
                null,
                null,
                SortOption.DESC,
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
                member.getId(),
                null,
                null,
                SortOption.DESC,
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
        Pageable pageable = PageRequest.of(0, 2);
        
        // when
        Page<ArticleResponse> result = articleService.getArticles(
                member.getId(),
                null,
                null,
                SortOption.ASC,
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
        ArticleDetailResponse result = articleService.getArticleDetail(article.getId(), member.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.title()).isEqualTo(article.getTitle());
            softly.assertThat(result.newsletter().name()).isEqualTo(newsletter.getName());
        });
    }

    @Test
    void 아티클_상세_조회_아티클이_존재하지_않으면_예외() {
        // when & then
        assertThatThrownBy(() -> articleService.getArticleDetail(0L, member.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 아티클_상세_조회_멤버가_존재하지_않으면_예외() {
        // given
        Article article = articles.getFirst();

        // when & then
        assertThatThrownBy(() -> articleService.getArticleDetail(article.getId(), 0L))
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
                .arrivedDateTime(baseTime)
                .build();
        articleRepository.save(article);

        // when & then
        assertThatThrownBy(() -> articleService.getArticleDetail(article.getId(), member.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 아티클_상세_조회_카테고리가_존재하지_않으면_예외() {
        // given
        Newsletter newsletter = Newsletter.builder()
                .name("테스트 뉴스레터")
                .description("테스트 설명")
                .imageUrl("https://example.com/test.png")
                .email("test@example.com")
                .categoryId(0L) // 존재하지 않는 카테고리 ID
                .detailId(0L)
                .build();
        newsletterRepository.save(newsletter);
        Article article = Article.builder()
                .title("테스트 아티클")
                .contents("<p>테스트 내용</p>")
                .thumbnailUrl("https://example.com/test.png")
                .expectedReadTime(3)
                .contentsSummary("테스트 요약")
                .isRead(false)
                .memberId(member.getId())
                .newsletterId(newsletter.getId())
                .arrivedDateTime(baseTime)
                .build();
        articleRepository.save(article);

        // when & then
        assertThatThrownBy(() -> articleService.getArticleDetail(article.getId(), member.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }
}
