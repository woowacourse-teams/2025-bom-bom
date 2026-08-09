package me.bombom.api.v1.badge.repository;

import me.bombom.api.v1.badge.domain.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

    int countByMemberId(Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM Badge b
            WHERE b.memberId = :memberId
            """)
    void bulkDeleteAllByMemberId(Long memberId);
}
