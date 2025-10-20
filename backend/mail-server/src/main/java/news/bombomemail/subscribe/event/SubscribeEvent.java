package news.bombomemail.subscribe.event;

public record SubscribeEvent(
        Long newsletterId,
        Long memberId,
        String unsubscribeUrl
) {

    public static SubscribeEvent of(Long newsletterId, Long memberId, String unsubscribeUrl) {
        return new SubscribeEvent(newsletterId, memberId, unsubscribeUrl);
    }
}
