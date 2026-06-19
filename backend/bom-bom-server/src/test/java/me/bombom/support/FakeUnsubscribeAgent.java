package me.bombom.support;

import java.util.ArrayList;
import java.util.List;
import me.bombom.api.v1.subscribe.service.UnsubscribeAgent;

public class FakeUnsubscribeAgent extends UnsubscribeAgent implements ResettableTestDouble {

    private final List<UnsubscribeRequest> requests = new ArrayList<>();
    private RuntimeException failure;

    public FakeUnsubscribeAgent() {
        super(null, null);
    }

    @Override
    public void unsubscribe(String url, Long newsletterId) {
        requests.add(new UnsubscribeRequest(url, newsletterId));
        if (failure != null) {
            throw failure;
        }
    }

    public void failWith(RuntimeException failure) {
        this.failure = failure;
    }

    public List<UnsubscribeRequest> getRequests() {
        return List.copyOf(requests);
    }

    @Override
    public void reset() {
        requests.clear();
        failure = null;
    }

    public record UnsubscribeRequest(String url, Long newsletterId) {
    }
}
