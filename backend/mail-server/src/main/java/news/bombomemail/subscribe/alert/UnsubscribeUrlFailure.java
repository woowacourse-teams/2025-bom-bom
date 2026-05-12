package news.bombomemail.subscribe.alert;

import news.bombomemail.subscribe.event.UnsubscribeUrlMissingEvent;

public record UnsubscribeUrlFailure(

        Long newsletterId,
        String newsletterName,
        String articleTitle
) {

    public static UnsubscribeUrlFailure from(UnsubscribeUrlMissingEvent event) {
        return new UnsubscribeUrlFailure(event.newsletterId(), event.newsletterName(), event.articleTitle());
    }
}
