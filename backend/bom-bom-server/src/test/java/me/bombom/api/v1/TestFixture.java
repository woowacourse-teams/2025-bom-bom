package me.bombom.api.v1;

import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.member.domain.ContinueReading;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.domain.TodayReading;
import me.bombom.api.v1.member.domain.WeeklyReading;
import me.bombom.api.v1.member.enums.Gender;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;

public final class TestFixture {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2025, 7, 15, 10, 0);

    private TestFixture(){}

    /**
     * Member
     */
    public static Member normalMemberFixture(){
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
     * Article
     */
    public static List<Article> createArticles(Member member, List<Newsletter> newsletters) {
        return List.of(
                createArticle(member.getId(), newsletters.get(0).getId(), BASE_TIME.minusMinutes(5)),
                createArticle(member.getId(), newsletters.get(1).getId(), BASE_TIME.minusMinutes(10)),
                createArticle(member.getId(), newsletters.get(2).getId(), BASE_TIME.minusMinutes(20)),
                createArticle(member.getId(), newsletters.get(2).getId(), BASE_TIME.minusDays(1))
        );
    }

    public static Article createArticle(Long memberId, Long newsletterId, LocalDateTime arrivedTime) {
        return Article.builder()
                .title("아티클")
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
    public static ContinueReading continueReadingFixture(Member member){
        return ContinueReading.builder()
                .memberId(member.getId())
                .dayCount(10)
                .build();
    }

    /**
     * TodayReading
     */
    public static TodayReading todayReadingFixture(Member member){
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
}
