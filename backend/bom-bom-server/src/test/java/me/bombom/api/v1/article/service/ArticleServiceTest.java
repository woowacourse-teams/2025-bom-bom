package me.bombom.api.v1.article.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.bookmark.domain.Bookmark;
import me.bombom.api.v1.bookmark.repository.BookmarkRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.domain.Role;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.member.repository.RoleRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterDetailRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.support.integration.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
    private RoleRepository roleRepository;

    @Test
    void 북마크되지_않은_아티클만_초과_정리한다() {
        // given
        Member member = saveUserMember();
        Newsletter newsletter = saveArticleNewsletter("아티클정리");
        Article bookmarkedArticle = saveBookmarkedArticle(member, newsletter);
        saveUnbookmarkedArticles(member, newsletter, 505);

        Set<Long> bookmarkedArticleIds = Set.of(bookmarkedArticle.getId());
        long unbookmarkedBefore = articleRepository.findAll().stream()
                .filter(article -> article.getMemberId().equals(member.getId()))
                .filter(article -> !bookmarkedArticleIds.contains(article.getId()))
                .count();

        // when
        int deletedCount = articleService.cleanupExcessArticles(1000, 500);

        // then
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

    private Member saveUserMember() {
        Role userRole = roleRepository.save(Role.builder()
                .authority("USER")
                .build());
        Member member = TestFixture.createMemberWithRole(
                uniqueValue("member"),
                uniqueValue("user"),
                userRole.getId()
        );
        return memberRepository.save(member);
    }

    private Newsletter saveArticleNewsletter(String name) {
        Category category = categoryRepository.save(Category.builder()
                .name(name + "카테고리")
                .build());
        NewsletterDetail detail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(false));
        Newsletter newsletter = TestFixture.createNewsletter(
                name,
                uniqueEmail("article"),
                category.getId(),
                detail.getId()
        );
        return newsletterRepository.save(newsletter);
    }

    private Article saveBookmarkedArticle(Member member, Newsletter newsletter) {
        Article article = articleRepository.save(TestFixture.createArticle(
                "북마크 아티클",
                member.getId(),
                newsletter.getId(),
                BASE_TIME.minusDays(100)
        ));
        bookmarkRepository.save(Bookmark.builder()
                .articleId(article.getId())
                .memberId(member.getId())
                .build());
        return article;
    }

    private void saveUnbookmarkedArticles(Member member, Newsletter newsletter, int count) {
        List<Article> articles = IntStream.range(0, count)
                .mapToObj(index -> TestFixture.createArticle(
                        "bulk " + index,
                        member.getId(),
                        newsletter.getId(),
                        BASE_TIME.plusMinutes(index)
                ))
                .toList();
        articleRepository.saveAll(articles);
    }

    private String uniqueEmail(String prefix) {
        return uniqueValue(prefix) + "@bombom.news";
    }

    private String uniqueValue(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
