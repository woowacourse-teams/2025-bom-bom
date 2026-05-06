package news.bombomemail.subscribe.alert;

import news.bombomemail.subscribe.event.UnsubscribeUrlMissingEvent;

public record UnsubscribeUrlFailure(

        String newsletterName,
        String articleTitle
) {
    public static UnsubscribeUrlFailure from(UnsubscribeUrlMissingEvent event) {
        return new UnsubscribeUrlFailure(event.newsletterName(), event.articleTitle());
    }
}
