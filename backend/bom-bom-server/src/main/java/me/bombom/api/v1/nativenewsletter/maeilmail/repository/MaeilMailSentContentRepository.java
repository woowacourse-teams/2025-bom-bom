package me.bombom.api.v1.nativenewsletter.maeilmail.repository;

import java.util.List;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSentContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MaeilMailSentContentRepository extends JpaRepository<MaeilMailSentContent, Long> {

    @Query("SELECT s.contentId FROM MaeilMailSentContent s WHERE s.memberId = :memberId AND s.topicId = :topicId")
    List<Long> findContentIdsByMemberIdAndTopicId(
            @Param("memberId") Long memberId,
            @Param("topicId") Long topicId
    );

    @Modifying
    void deleteByMemberIdAndTopicId(Long memberId, Long topicId);
}
