package news.bombomemail.subscribe.alert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import news.bombomemail.subscribe.event.UnsubscribeUrlMissingEvent;

public class PendingUnsubscribeFailures {

    private final ConcurrentHashMap<Long, UnsubscribeUrlFailure> failures = new ConcurrentHashMap<>();

    void record(UnsubscribeUrlMissingEvent event) {
        failures.putIfAbsent(event.newsletterId(), UnsubscribeUrlFailure.from(event));
    }

    List<UnsubscribeUrlFailure> collectForAlert() {
        List<UnsubscribeUrlFailure> collectedFailures = new ArrayList<>();

        for (ConcurrentHashMap.Entry<Long, UnsubscribeUrlFailure> entry : failures.entrySet()) {
            if (failures.remove(entry.getKey(), entry.getValue())) {
                collectedFailures.add(entry.getValue());
            }
        }
        return collectedFailures;
    }
}
