package me.bombom.api.v1.article.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.domain.Article;
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

    List<Category> categories;
    List<Newsletter> newsletters;
    List<Article> articles;
    Member member;

    @Autowired
    private MemberRepository memberRepository;

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
                        .articleUrl("https://example.com/articles/dev-tools")
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
                        .articleUrl("https://example.com/articles/ai-life")
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
                        .articleUrl("https://example.com/articles/it-trend-2025")
                        .thumbnailUrl("https://example.com/images/it-trend.png")
                        .expectedReadTime(4)
                        .contentsSummary("다가오는 IT 트렌드를 전망하고 주요 기술을 짚어봅니다.")
                        .isRead(false)
                        .memberId(member.getId())
                        .newsletterId(newsletters.get(2).getId())
                        .arrivedDateTime(baseTime.minusMinutes(20))
                        .build()
        );
        articleRepository.saveAll(articles);
    }

    @Test
    void 오늘의_뉴스레터_조회_DESC_정렬_테스트() {
        //when
        List<ArticleResponse> result = articleService.getArticles(
                member.getId(),
                baseTime.toLocalDate(),
                null,
                SortOption.DESC
        );

        //then
        assertAll(
                () -> assertThat(result).hasSize(newsletters.size()),
                () -> assertThat(result.get(0).arrivedDateTime()).isAfter(result.get(1).arrivedDateTime()),
                () -> assertThat(result.get(1).arrivedDateTime()).isAfter(result.get(2).arrivedDateTime())
        );
    }

    @Test
    void 오늘의_뉴스레터_조회_ASC_정렬_테스트() {
        //when
        List<ArticleResponse> result = articleService.getArticles(
                member.getId(),
                null,
                null,
                SortOption.ASC
        );

        //then
        assertAll(
                () -> assertThat(result).hasSize(newsletters.size()),
                () -> assertThat(result.get(0).arrivedDateTime()).isBefore(result.get(1).arrivedDateTime()),
                () -> assertThat(result.get(1).arrivedDateTime()).isBefore(result.get(2).arrivedDateTime())
        );
    }

    @Test
    void 전체_카테고리_조회_테스트() {
        //when
        List<ArticleResponse> result = articleService.getArticles(
                member.getId(),
                null,
                categories.getFirst().getName(),
                SortOption.DESC
        );

        //then
        assertThat(result).hasSize(1);
    }

    @Test
    void 아티클_목록_조회_멤버가_존재하지_않으면_예외() {
        assertThatThrownBy(() -> articleService.getArticles(
                0L,
                null,
                categories.getFirst().getName(),
                SortOption.DESC
        )).isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 아티클_목록_조회_카테고리가_존재하지_않으면_예외() {
        assertThatThrownBy(() -> articleService.getArticles(
                member.getId(),
                null,
                "Invalid Category",
                SortOption.DESC
        )).isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }
}