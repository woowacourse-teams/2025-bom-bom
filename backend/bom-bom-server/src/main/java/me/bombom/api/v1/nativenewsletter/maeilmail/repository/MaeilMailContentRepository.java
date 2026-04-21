package me.bombom.api.v1.nativenewsletter.maeilmail.repository;

import java.util.List;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MaeilMailContentRepository extends JpaRepository<MaeilMailContent, Long> {

    @Query("SELECT c.id FROM MaeilMailContent c WHERE c.topicId = :topicId")
    List<Long> findIdsByTopicId(@Param("topicId") Long topicId);
}
