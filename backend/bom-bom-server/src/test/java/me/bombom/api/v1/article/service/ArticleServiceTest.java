package me.bombom.api.v1.article.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.bookmark.domain.Bookmark;
import me.bombom.api.v1.bookmark.repository.BookmarkRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.enums.Gender;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterDetailRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.support.integration.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@IntegrationTest
class ArticleServiceTest {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2025, 7, 15, 10, 0);

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private NewsletterDetailRepository newsletterDetailRepository;

    @Autowired
    private NewsletterRepository newsletterRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Member member;
    private Newsletter newsletter;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("""
                INSERT INTO role (id, authority)
                VALUES (1, 'USER')
                ON DUPLICATE KEY UPDATE authority = 'USER'
                """);

        member = memberRepository.save(Member.builder()
                .provider("apple")
                .providerId("article-cleanup-test")
                .email("article-cleanup@bombom.news")
                .nickname("cleanup")
                .gender(Gender.FEMALE)
                .roleId(1L)
                .build());
        Category category = categoryRepository.save(TestFixture.createCategories().getFirst());
        NewsletterDetail detail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(false));
        newsletter = newsletterRepository.save(TestFixture.createNewsletter(
                "정리 테스트 뉴스레터",
                "cleanup@example.com",
                category.getId(),
                detail.getId()
        ));
    }

    @Test
    void 북마크되지_않은_아티클만_초과_정리한다() {
        Article bookmarkedArticle = articleRepository.save(TestFixture.createArticle(
                "북마크 아티클",
                member.getId(),
                newsletter.getId(),
                BASE_TIME.minusDays(100)
        ));
        bookmarkRepository.save(Bookmark.builder()
                .articleId(bookmarkedArticle.getId())
                .memberId(member.getId())
                .build());

        List<Article> additionalArticles = IntStream.range(0, 505)
                .mapToObj(index -> TestFixture.createArticle(
                        "bulk " + index,
                        member.getId(),
                        newsletter.getId(),
                        BASE_TIME.plusMinutes(index)
                ))
                .toList();
        articleRepository.saveAll(additionalArticles);

        Set<Long> bookmarkedArticleIds = bookmarkRepository.findAll().stream()
                .map(Bookmark::getArticleId)
                .collect(Collectors.toSet());
        long unbookmarkedBefore = articleRepository.findAll().stream()
                .filter(article -> article.getMemberId().equals(member.getId()))
                .filter(article -> !bookmarkedArticleIds.contains(article.getId()))
                .count();

        int deletedCount = articleService.cleanupExcessArticles(1000, 500);

        List<Article> remainingArticles = articleRepository.findAll().stream()
                .filter(article -> article.getMemberId().equals(member.getId()))
                .toList();
        long unbookmarkedAfter = remainingArticles.stream()
                .filter(article -> !bookmarkedArticleIds.contains(article.getId()))
                .count();

        assertSoftly(softly -> {
            softly.assertThat(deletedCount).isEqualTo(unbookmarkedBefore - 500);
            softly.assertThat(unbookmarkedAfter).isEqualTo(500);
            softly.assertThat(remainingArticles)
                    .extracting(Article::getId)
                    .contains(bookmarkedArticle.getId());
        });
    }
}
