package me.bombom.api.v1.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppleAuthorizationRequest {

    @Id
    @Column(length = 64)
    private String requestKey;

    @Column(nullable = false, length = 512)
    private String state;

    @Lob
    @Column(nullable = false, columnDefinition = "MEDIUMBLOB")
    private byte[] requestBytes;

    @Column(nullable = false)
    private Instant expiresAt;

    @Builder
    private AppleAuthorizationRequest(String requestKey, String state, byte[] requestBytes, Instant expiresAt) {
        this.requestKey = requestKey;
        this.state = state;
        this.requestBytes = requestBytes;
        this.expiresAt = expiresAt;
    }
}
