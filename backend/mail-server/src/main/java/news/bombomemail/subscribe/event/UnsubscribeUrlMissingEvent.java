package news.bombomemail.subscribe.event;

public record UnsubscribeUrlMissingEvent(

        Long newsletterId,
        String newsletterName,
        String articleTitle
) {}
