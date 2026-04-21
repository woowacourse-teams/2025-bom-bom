package me.bombom.api.v1.nativenewsletter.maeilmail.repository;

import java.util.List;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSubscriptionTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MaeilMailSubscriptionTrackRepository extends JpaRepository<MaeilMailSubscriptionTrack, Long> {

    @Query("""
            SELECT t FROM MaeilMailSubscriptionTrack t
            JOIN Subscribe s ON s.id = t.subscribeId
            WHERE s.status = 'SUBSCRIBED'
            """)
    List<MaeilMailSubscriptionTrack> findAllActiveWithSubscribe();

    @Modifying
    @Query("""
            UPDATE MaeilMailSubscriptionTrack t
            SET t.curriculumIndex = t.curriculumIndex + 1
            WHERE t.id IN :ids
            """)
    void incrementCurriculumIndexByIds(@Param("ids") List<Long> ids);
}
