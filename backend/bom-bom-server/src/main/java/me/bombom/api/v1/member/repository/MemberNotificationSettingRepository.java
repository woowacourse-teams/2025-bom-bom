package me.bombom.api.v1.member.repository;

import me.bombom.api.v1.member.domain.MemberNotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberNotificationSettingRepository extends JpaRepository<MemberNotificationSetting, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM MemberNotificationSetting s
            WHERE s.memberId = :memberId
            """)
    void bulkDeleteAllByMemberId(@Param("memberId") Long memberId);
}
