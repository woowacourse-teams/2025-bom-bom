package news.bombomemail.subscribe.event;

public record NewsletterSubscribedEvent(Long newsletterId, Long memberId) {

    public static NewsletterSubscribedEvent of(Long newsletterId, Long memberId) {
        return new NewsletterSubscribedEvent(newsletterId, memberId);
    }
}
