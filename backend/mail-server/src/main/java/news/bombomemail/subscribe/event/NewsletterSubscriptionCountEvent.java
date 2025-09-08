package news.bombomemail.subscribe.event;

public record NewsletterSubscriptionCountEvent(Long newsletterId, Long memberId) {

    public static NewsletterSubscriptionCountEvent of(Long newsletterId, Long memberId) {
        return new NewsletterSubscriptionCountEvent(newsletterId, memberId);
    }
}
