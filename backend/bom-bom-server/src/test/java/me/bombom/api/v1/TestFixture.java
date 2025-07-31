package me.bombom.api.v1;

import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.highlight.domain.Highlight;
import me.bombom.api.v1.highlight.domain.HighlightLocation;
import me.bombom.api.v1.highlight.dto.request.HighlightCreateRequest;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.enums.Gender;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.reading.domain.ContinueReading;
import me.bombom.api.v1.reading.domain.TodayReading;
import me.bombom.api.v1.reading.domain.WeeklyReading;

public final class TestFixture {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2025, 7, 15, 10, 0);

    private TestFixture() {
    }

    /**
     * Member
     */
    public static Member normalMemberFixture() {
        return Member.builder()
                .provider("provider")
                .providerId("providerId")
                .email("email")
                .nickname("nickname")
                .gender(Gender.FEMALE)
                .roleId(1L)
                .build();
    }

    /**
     * Category
     */
    public static List<Category> createCategories() {
        return List.of(
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
    }

    /**
     * Newsletter
     */
    public static List<Newsletter> createNewsletters(List<Category> categories) {
        return List.of(
                createNewsletter("뉴스픽", "news@newspick.com", categories.get(0).getId()),
                createNewsletter("IT타임즈", "editor@ittimes.io", categories.get(1).getId()),
                createNewsletter("비즈레터", "biz@biz.com", categories.get(2).getId())
        );
    }

    public static Newsletter createNewsletter(String name, String email, Long categoryId) {
        return Newsletter.builder()
                .name(name)
                .description("설명")
                .imageUrl("https://cdn.bombom.me/img.png")
                .email(email)
                .categoryId(categoryId)
                .detailId(1L)
                .build();
    }

    /**
     * NewsletterDetail
     */
    public static List<NewsletterDetail> createNewsletterDetails() {
        return List.of(
                NewsletterDetail.builder()
                        .mainPageUrl("https://news1.com")
                        .subscribeUrl("https://news1.com/subscribe")
                        .issueCycle("매일 발행")
                        .subscribeCount(1000)
                        .build(),
                NewsletterDetail.builder()
                        .mainPageUrl("https://ittimes.com")
                        .subscribeUrl("https://ittimes.com/subscribe")
                        .issueCycle("매주 월요일")
                        .subscribeCount(850)
                        .build(),
                NewsletterDetail.builder()
                        .mainPageUrl("https://biz.com")
                        .subscribeUrl("https://biz.com/subscribe")
                        .issueCycle("격주 화요일")
                        .subscribeCount(600)
                        .build()
        );
    }

    /**
     * Article
     */
    public static List<Article> createArticles(Member member, List<Newsletter> newsletters) {
        return List.of(
                createArticle("뉴스", member.getId(), newsletters.get(0).getId(), BASE_TIME.minusMinutes(5)),
                createArticle("뉴스", member.getId(), newsletters.get(1).getId(), BASE_TIME.minusMinutes(10)),
                createArticle("레터", member.getId(), newsletters.get(2).getId(), BASE_TIME.minusMinutes(20)),
                createArticle("레터", member.getId(), newsletters.get(2).getId(), BASE_TIME.minusDays(1))
        );
    }

    public static Article createArticle(String title, Long memberId, Long newsletterId, LocalDateTime arrivedTime) {
        return Article.builder()
                .title(title)
                .contents("<h1>아티클</h1>")
                .thumbnailUrl("https://example.com/images/thumb.png")
                .expectedReadTime(5)
                .contentsSummary("요약")
                .isRead(false)
                .memberId(memberId)
                .newsletterId(newsletterId)
                .arrivedDateTime(arrivedTime)
                .build();
    }

    /**
     * ContinueReading
     */
    public static ContinueReading continueReadingFixture(Member member) {
        return ContinueReading.builder()
                .memberId(member.getId())
                .dayCount(10)
                .build();
    }

    /**
     * TodayReading
     */
    public static TodayReading todayReadingFixture(Member member) {
        return TodayReading.builder()
                .memberId(member.getId())
                .currentCount(1)
                .totalCount(3)
                .build();
    }

    /**
     * WeeklyReading
     */
    public static WeeklyReading weeklyReadingFixture(Member member) {
        return WeeklyReading.builder()
                .memberId(member.getId())
                .currentCount(3)
                .goalCount(5)
                .build();
    }

    /**
     * Highlight
     */

    public static List<Highlight> createHighlightFixtures(List<Article> articles) {
        Long firstArticleId = articles.get(0).getId();
        Long secondArticleId = articles.get(1).getId();
        return List.of(
                Highlight.builder()
                        .highlightLocation(new HighlightLocation("0", "div[0]/p[0]", "10", "div[0]/p[0]"))
                        .articleId(firstArticleId)
                        .color("#ffeb3b")
                        .text("첫 번째 하이라이트")
                        .build(),
                Highlight.builder()
                        .highlightLocation(new HighlightLocation("15", "div[0]/p[1]", "25", "div[0]/p[1]"))
                        .articleId(firstArticleId)
                        .color("#4caf50")
                        .text("두 번째 하이라이트")
                        .build(),
                Highlight.builder()
                        .highlightLocation(new HighlightLocation("5", "div[0]/h1", "15", "div[0]/h1"))
                        .articleId(secondArticleId)
                        .color("#2196f3")
                        .text("세 번째 하이라이트")
                        .build()
        );
    }

    public static HighlightCreateRequest createHighlightRequest(Long articleId) {
        return new HighlightCreateRequest(
                "0",
                "div[0]/p[2]",
                "20",
                "div[0]/p[2]",
                articleId,
                "#f44336",
                "새로운 하이라이트 텍스트"
        );
    }
}
