package news.bombomemail.article.event;

public record ArticleArrivedEvent(
        Long newsletterId,
        Long memberId,
        String unsubscribeUrl
) {

    public static ArticleArrivedEvent of(Long newsletterId, Long memberId, String unsubscribeUrl) {
        return new ArticleArrivedEvent(newsletterId, memberId, unsubscribeUrl);
    }
}
