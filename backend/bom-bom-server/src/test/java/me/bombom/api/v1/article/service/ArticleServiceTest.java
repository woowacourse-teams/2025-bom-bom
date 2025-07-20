package me.bombom.api.v1.article.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.dto.ArticleDetailResponse;
import me.bombom.api.v1.article.dto.ArticleResponse;
import me.bombom.api.v1.article.dto.GetArticleCategoryStatisticsResponse;
import me.bombom.api.v1.article.dto.GetArticlesOptions;
import me.bombom.api.v1.article.enums.SortOption;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.enums.Gender;
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
        categories = TestFixture.createCategories();
        categoryRepository.saveAll(categories);
        newsletters = TestFixture.createNewsletters(categories);
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
                member.getId(),
                GetArticlesOptions.of(null, null, SortOption.DESC),
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
                GetArticlesOptions.of(null, null, SortOption.ASC),
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
        Category category = categories.getFirst();
        Long categoryId = category.getId();

        // when
        Page<ArticleResponse> result = articleService.getArticles(
                member.getId(),
                GetArticlesOptions.of(null, categoryId, SortOption.DESC),
                pageable
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.getTotalElements()).isEqualTo(1);
            softly.assertThat(result.getContent()).hasSize(1);
            softly.assertThat(result.getContent()).extracting("newsletter")
                    .extracting("category")
                    .containsExactly(category.getName());
        });
    }

    @Test
    void 아티클_목록_조회_날짜_필터링_테스트() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<ArticleResponse> result = articleService.getArticles(
                member.getId(),
                GetArticlesOptions.of(baseTime.toLocalDate(), null, SortOption.DESC),
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
                GetArticlesOptions.of(null, categories.getFirst().getId(), SortOption.DESC),
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
                GetArticlesOptions.of(null, 0L, SortOption.DESC),
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
                GetArticlesOptions.of(null, null, SortOption.DESC),
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
                GetArticlesOptions.of(null, null, SortOption.DESC),
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
                GetArticlesOptions.of(null, null, SortOption.DESC),
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
                GetArticlesOptions.of(null, null, SortOption.ASC),
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
        Newsletter newsletter = TestFixture.createNewsletter("테스트 뉴스레터", "test@example.com", 0L);
        newsletterRepository.save(newsletter);
        Article article = TestFixture.createArticle(member.getId(), newsletter.getId(), baseTime);
        articleRepository.save(article);

        // when & then
        assertThatThrownBy(() -> articleService.getArticleDetail(article.getId(), member.getId()))
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
        assertThatThrownBy(() -> articleService.getArticleDetail(articles.getFirst().getId(), member2.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.FORBIDDEN_RESOURCE);
    }

    @Test
    void 다_읽음_갱신_아티클_읽음_갱신_성공_테스트() {
        // given
        Article article = TestFixture.createArticle(member.getId(), newsletters.getFirst().getId(), baseTime);
        articleRepository.save(article);

        //when
        articleService.markAsRead(article.getId());

        // then
        assertThat(article.isRead()).isTrue();
    }

    @Test
    void 다_읽음_갱신_아티클이_존재하지_않으면_예외() {
        // when & then
        assertThatThrownBy(() -> articleService.markAsRead(0L))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 카테고리_별_아티클_개수를_조회한다() {
        // when
        GetArticleCategoryStatisticsResponse result = articleService.getArticleCategoryStatistics(member.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.totalCount()).isEqualTo(4);
            softly.assertThat(result.categories()).hasSize(3);
            softly.assertThat(result.categories().get(1).category()).isEqualTo("테크");
            softly.assertThat(result.categories().get(1).count()).isEqualTo(1);
            softly.assertThat(result.categories().get(2).category()).isEqualTo("푸드");
            softly.assertThat(result.categories().get(2).count()).isEqualTo(2);
        });
    }

    @Test
    void 카테고리_별_아티클_개수_조회_시_회원이_존재하지_않을_경우_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> articleService.getArticleCategoryStatistics(2L))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }
}
