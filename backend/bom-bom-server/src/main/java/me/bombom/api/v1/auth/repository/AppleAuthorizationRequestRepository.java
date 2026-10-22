package me.bombom.api.v1.auth.repository;

import java.time.Instant;
import java.util.Optional;
import me.bombom.api.v1.auth.domain.AppleAuthorizationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppleAuthorizationRequestRepository extends JpaRepository<AppleAuthorizationRequest, String> {

    // MySQL 문자열 비교의 뒤 공백 무시를 방지한다.
    @Query(value = """
            SELECT * FROM apple_authorization_request
            WHERE request_key = :key AND CAST(state AS BINARY) = CAST(:state AS BINARY) AND expires_at > :now
            """, nativeQuery = true)
    Optional<AppleAuthorizationRequest> findPending(
            @Param("key") String key, @Param("state") String state, @Param("now") Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM AppleAuthorizationRequest r WHERE r.requestKey = :key AND r.expiresAt > :now")
    int consume(@Param("key") String key, @Param("now") Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM AppleAuthorizationRequest r WHERE r.expiresAt <= :now")
    int deleteExpired(@Param("now") Instant now);
}
