package news.bombomemail.subscribe.alert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import news.bombomemail.subscribe.event.UnsubscribeUrlMissingEvent;

public class PendingUnsubscribeFailures {

    private final Map<Long, UnsubscribeUrlFailure> failures = new ConcurrentHashMap<>();

    void record(UnsubscribeUrlMissingEvent event) {
        failures.putIfAbsent(event.newsletterId(), UnsubscribeUrlFailure.from(event));
    }

    List<UnsubscribeUrlFailure> collectForAlert() {
        List<UnsubscribeUrlFailure> result = new ArrayList<>(failures.values());
        failures.clear();
        return result;
    }
}
