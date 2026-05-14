package me.bombom.api.v1.member.repository;

import java.util.Optional;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("""
        SELECT m
        FROM Member m
        WHERE m.provider = :provider AND m.providerId = :providerId
    """)
    Optional<Member> findByProviderAndProviderId(@Param("provider") String provider, @Param("providerId") String providerId);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    long countByRoleId(long roleId);

    @Query("""
        SELECT m.id
        FROM Member m
        JOIN Role r ON m.roleId = r.id
        WHERE r.authority = 'ARCHIVE'
    """)
    Optional<Long> findArchiveAdminId();

    @Query("""
        SELECT COUNT(m) > 0
        FROM Member m
        JOIN Role r ON m.roleId = r.id
        WHERE m.id = :memberId
          AND r.authority = :authority
    """)
    boolean existsByIdAndRoleAuthority(@Param("memberId") Long memberId, @Param("authority") String authority);

}
