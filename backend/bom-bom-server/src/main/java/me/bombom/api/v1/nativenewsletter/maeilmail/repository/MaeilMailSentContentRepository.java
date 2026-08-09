package me.bombom.api.v1.nativenewsletter.maeilmail.repository;

import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSentContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MaeilMailSentContentRepository extends JpaRepository<MaeilMailSentContent, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM MaeilMailSentContent c
            WHERE c.memberId = :memberId
            """)
    void bulkDeleteAllByMemberId(@Param("memberId") Long memberId);
}
