package me.bombom.api.v1.withdraw.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.withdraw.domain.WithdrawnMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WithdrawnMemberRepository extends JpaRepository<WithdrawnMember, Long> {

    Optional<WithdrawnMember> findByMemberId(Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM WithdrawnMember wm
            WHERE wm.expireDate <= :date
              AND wm.cleanupCompleted = true
            """)
    void bulkDeleteAllExpiredAndCleanupCompleted(@Param("date") LocalDate date);

    @Query("""
            SELECT wm.memberId
            FROM WithdrawnMember wm
            WHERE wm.cleanupCompleted = false
            """)
    List<Long> findAllMemberIdsByCleanupNotCompleted();
}
