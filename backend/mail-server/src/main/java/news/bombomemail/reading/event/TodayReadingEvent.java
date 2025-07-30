package news.bombomemail.reading.event;

public record TodayReadingEvent(Long memberId) {

    public static TodayReadingEvent from (Long memberId) {
        return new TodayReadingEvent(memberId);
    }
}
