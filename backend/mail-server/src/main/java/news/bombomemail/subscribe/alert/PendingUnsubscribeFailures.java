package news.bombomemail.subscribe.alert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import news.bombomemail.subscribe.event.UnsubscribeUrlMissingEvent;

public class PendingUnsubscribeFailures {

    private final AtomicReference<Map<Long, UnsubscribeUrlFailure>> failures = new AtomicReference<>(new ConcurrentHashMap<>());

    void record(UnsubscribeUrlMissingEvent event) {
        failures.get().putIfAbsent(event.newsletterId(), UnsubscribeUrlFailure.from(event));
    }

    List<UnsubscribeUrlFailure> collectForAlert() {
        Map<Long, UnsubscribeUrlFailure> snapshot = failures.getAndSet(new ConcurrentHashMap<>());
        return new ArrayList<>(snapshot.values());
    }
}
