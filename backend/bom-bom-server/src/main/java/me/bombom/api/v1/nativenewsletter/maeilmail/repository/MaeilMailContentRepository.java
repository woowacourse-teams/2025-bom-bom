package me.bombom.api.v1.nativenewsletter.maeilmail.repository;

import java.util.Collection;
import java.util.List;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailContent;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.TopicContentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MaeilMailContentRepository extends JpaRepository<MaeilMailContent, Long> {

    @Query("""
            SELECT new me.bombom.api.v1.nativenewsletter.maeilmail.dto.TopicContentId(c.topicId, c.id)
            FROM MaeilMailContent c
            WHERE c.topicId IN :topicIds
            """)
    List<TopicContentId> findContentIdsByTopicIdIn(@Param("topicIds") Collection<Long> topicIds);
}
