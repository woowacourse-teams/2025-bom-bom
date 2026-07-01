package me.bombom.api.v1.nativenewsletter.maeilmail.repository;

import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailUserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MaeilMailUserAnswerRepository extends JpaRepository<MaeilMailUserAnswer, Long> {

    Optional<MaeilMailUserAnswer> findByMemberIdAndIssueHistoryId(Long memberId, Long issueHistoryId);

    // TODO: 모든 사용자 답변 리스트로 변경
    List<MaeilMailUserAnswer> findByMemberIdAndIssueHistoryIdIn(Long memberId, Collection<Long> issueHistoryIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM MaeilMailUserAnswer a
            WHERE a.memberId = :memberId
            """)
    void bulkDeleteAllByMemberId(@Param("memberId") Long memberId);
}
