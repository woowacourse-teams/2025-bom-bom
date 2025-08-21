package me.bombom.api.v1.member.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query(value = """
                SELECT * FROM member
                WHERE provider = :provider AND provider_id = :providerId
            """, nativeQuery = true)
    Optional<Member> findByProviderAndProviderIdIncludeDeleted(
            @Param("provider") String provider,
            @Param("providerId") String providerId
    );

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    void deleteByDeletedAtBefore(LocalDateTime threshold);
}
