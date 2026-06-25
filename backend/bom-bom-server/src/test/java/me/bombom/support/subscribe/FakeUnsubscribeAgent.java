package me.bombom.support.subscribe;

import java.util.ArrayList;
import java.util.List;
import me.bombom.api.v1.subscribe.service.UnsubscribeAgent;
import me.bombom.support.testdouble.ResettableTestDouble;

/**
 * 구독 해지 통합 테스트에서 실제 외부 해지 요청을 보내지 않고 요청 내역과 실패 상황을 제어한다.
 */
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
