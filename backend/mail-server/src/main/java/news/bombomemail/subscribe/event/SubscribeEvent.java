package news.bombomemail.subscribe.event;

public record SubscribeEvent(Long newsletterId, Long memberId) {

    public static SubscribeEvent of(Long newsletterId, Long memberId) {
        return new SubscribeEvent(newsletterId, memberId);
    }
}
