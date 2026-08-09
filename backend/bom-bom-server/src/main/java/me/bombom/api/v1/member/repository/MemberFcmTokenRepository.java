package me.bombom.api.v1.member.repository;

import me.bombom.api.v1.member.domain.MemberFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberFcmTokenRepository extends JpaRepository<MemberFcmToken, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM MemberFcmToken t
            WHERE t.memberId = :memberId
            """)
    void bulkDeleteAllByMemberId(@Param("memberId") Long memberId);
}
