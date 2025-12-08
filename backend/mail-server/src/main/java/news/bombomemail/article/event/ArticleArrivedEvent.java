package news.bombomemail.article.event;

import jakarta.mail.internet.MimeMessage;

public record ArticleArrivedEvent(
        Long newsletterId,
        String newsletterName,
        Long articleId,
        String articleTitle,
        Long memberId,
        String unsubscribeUrl,
        MimeMessage message,
        String contents
) {
    public static ArticleArrivedEvent of(
            Long newsletterId,
            String newsletterName,
            Long articleId,
            String articleTitle,
            Long memberId,
            String unsubscribeUrl,
            MimeMessage message,
            String contents
    ) {
        return new ArticleArrivedEvent(
                newsletterId,
                newsletterName,
                articleId,
                articleTitle,
                memberId,
                unsubscribeUrl,
                message,
                contents
        );
    }
}
