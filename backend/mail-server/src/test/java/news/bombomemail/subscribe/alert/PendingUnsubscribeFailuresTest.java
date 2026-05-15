package news.bombomemail.subscribe.alert;

import java.util.List;
import news.bombomemail.subscribe.event.UnsubscribeUrlMissingEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PendingUnsubscribeFailuresTest {

    private PendingUnsubscribeFailures pendingFailures;

    @BeforeEach
    void setUp() {
        pendingFailures = new PendingUnsubscribeFailures();
    }

    @Test
    void 같은_뉴스레터는_중복_저장되지_않는다() {
        // given
        UnsubscribeUrlMissingEvent event1 = new UnsubscribeUrlMissingEvent(1L, "뉴스레터A", "아티클 1");
        UnsubscribeUrlMissingEvent event2 = new UnsubscribeUrlMissingEvent(1L, "뉴스레터A", "아티클 2");

        // when
        pendingFailures.record(event1);
        pendingFailures.record(event2);

        // then
        List<UnsubscribeUrlFailure> result = pendingFailures.collectForAlert();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).articleTitle()).isEqualTo("아티클 1");
    }

    @Test
    void 다른_뉴스레터는_각각_저장된다() {
        // given
        UnsubscribeUrlMissingEvent event1 = new UnsubscribeUrlMissingEvent(1L, "뉴스레터A", "아티클 1");
        UnsubscribeUrlMissingEvent event2 = new UnsubscribeUrlMissingEvent(2L, "뉴스레터B", "아티클 2");

        // when
        pendingFailures.record(event1);
        pendingFailures.record(event2);

        // then
        List<UnsubscribeUrlFailure> result = pendingFailures.collectForAlert();
        assertThat(result).hasSize(2);
    }

    @Test
    void collectForAlert_호출_후_초기화된다() {
        // given
        pendingFailures.record(new UnsubscribeUrlMissingEvent(1L, "뉴스레터A", "아티클 1"));

        // when
        pendingFailures.collectForAlert();
        List<UnsubscribeUrlFailure> result = pendingFailures.collectForAlert();

        // then
        assertThat(result).isEmpty();
    }
}
