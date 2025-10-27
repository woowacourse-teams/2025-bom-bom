package news.bombomemail.email.event;

public record ArticleArrivedEvent(
        Long memberId,
        String newsletterName,
        String articleTitle
) {

    public static ArticleArrivedEvent of(Long memberId, String newsletterName, String articleTitle) {
        return new ArticleArrivedEvent(memberId, newsletterName, articleTitle);
    }
}
