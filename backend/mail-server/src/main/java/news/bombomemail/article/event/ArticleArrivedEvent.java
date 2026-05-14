package news.bombomemail.article.event;

public record ArticleArrivedEvent(
        Long newsletterId,
        String newsletterName,
        Long articleId,
        String articleTitle,
        Long memberId,
        String unsubscribeUrl,
        String contents,
        ArticleSource source
) {
    public static ArticleArrivedEvent of(
            Long newsletterId,
            String newsletterName,
            Long articleId,
            String articleTitle,
            Long memberId,
            String unsubscribeUrl,
            String contents,
            ArticleSource source
    ) {
        return new ArticleArrivedEvent(
                newsletterId,
                newsletterName,
                articleId,
                articleTitle,
                memberId,
                unsubscribeUrl,
                contents,
                source
        );
    }
}
