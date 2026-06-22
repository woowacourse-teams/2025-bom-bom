package me.bombom.api.v1.article.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.dto.request.DeleteArticlesRequest;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.bookmark.domain.Bookmark;
import me.bombom.api.v1.bookmark.repository.BookmarkRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.exception.UnauthorizedException;
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
import me.bombom.api.v1.pet.ScorePolicyConstants;
import me.bombom.api.v1.reading.domain.TodayReading;
import me.bombom.api.v1.reading.repository.TodayReadingRepository;
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

    @Autowired
    private TodayReadingRepository todayReadingRepository;

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

    @Test
    void 오늘_읽기수가_정책_최대값_이하면_아티클_점수를_추가할_수_있다() {
        // given
        Member member = saveUserMember();
        todayReadingRepository.save(TodayReading.builder()
                .memberId(member.getId())
                .totalCount(5)
                .currentCount(ScorePolicyConstants.MAX_TODAY_READING_COUNT)
                .readCount(0)
                .build());

        // when
        boolean result = articleService.canAddArticleScore(member.getId());

        // then
        assertSoftly(softly -> softly.assertThat(result).isTrue());
    }

    @Test
    void 오늘_읽기수가_정책_최대값을_초과하면_아티클_점수를_추가할_수_없다() {
        // given
        Member member = saveUserMember();
        todayReadingRepository.save(TodayReading.builder()
                .memberId(member.getId())
                .totalCount(5)
                .currentCount(ScorePolicyConstants.MAX_TODAY_READING_COUNT + 1)
                .readCount(0)
                .build());

        // when
        boolean result = articleService.canAddArticleScore(member.getId());

        // then
        assertSoftly(softly -> softly.assertThat(result).isFalse());
    }

    @Test
    void 오늘_읽기정보가_없으면_아티클_점수_추가_가능여부를_판단할_수_없다() {
        // given
        Member member = saveUserMember();

        // when & then
        assertThatThrownBy(() -> articleService.canAddArticleScore(member.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 다른_회원의_아티클은_삭제할_수_없다() {
        // given
        Member owner = saveUserMember();
        Member requester = saveUserMember();
        Newsletter newsletter = saveArticleNewsletter("아티클삭제권한");
        Article article = articleRepository.save(TestFixture.createArticle(
                "삭제 대상",
                owner.getId(),
                newsletter.getId(),
                BASE_TIME
        ));

        // when & then
        assertThatThrownBy(() -> articleService.delete(requester, new DeleteArticlesRequest(List.of(article.getId()))))
                .isInstanceOf(UnauthorizedException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.FORBIDDEN_RESOURCE);
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
