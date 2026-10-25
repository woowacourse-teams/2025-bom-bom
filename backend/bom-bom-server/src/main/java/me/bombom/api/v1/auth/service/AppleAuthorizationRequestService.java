package me.bombom.api.v1.auth.service;

import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.auth.domain.AppleAuthorizationRequest;
import me.bombom.api.v1.auth.dto.PendingAppleAuthorizationRequest;
import me.bombom.api.v1.auth.repository.AppleAuthorizationRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.SerializationUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppleAuthorizationRequestService {

    private final AppleAuthorizationRequestRepository appleAuthorizationRequestRepository;
    private final Clock clock;

    @Transactional
    public void save(String key, PendingAppleAuthorizationRequest pending, Instant expiresAt) {
        appleAuthorizationRequestRepository.deleteExpired(clock.instant());
        appleAuthorizationRequestRepository.save(AppleAuthorizationRequest.builder()
                .requestKey(key)
                .state(pending.authorizationRequest().getState())
                .requestBytes(SerializationUtils.serialize(pending))
                .expiresAt(expiresAt)
                .build());
    }

    public PendingAppleAuthorizationRequest find(String key, String state, Instant now) {
        return appleAuthorizationRequestRepository.findPending(key, state, now)
                .map(request -> (PendingAppleAuthorizationRequest) SerializationUtils.deserialize(request.getRequestBytes()))
                .orElse(null);
    }

    @Transactional
    public boolean consume(String key, Instant now) {
        // DELETE 영향 행 수로 여러 서버에서도 한 요청만 소비하게 한다.
        return appleAuthorizationRequestRepository.consume(key, now) == 1;
    }
}
