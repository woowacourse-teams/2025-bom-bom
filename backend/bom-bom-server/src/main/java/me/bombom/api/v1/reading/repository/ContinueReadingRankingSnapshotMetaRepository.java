package me.bombom.api.v1.reading.repository;

import me.bombom.api.v1.reading.domain.ContinueReadingRankingSnapshotMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContinueReadingRankingSnapshotMetaRepository extends JpaRepository<ContinueReadingRankingSnapshotMeta, Long> {

    @Modifying(clearAutomatically = true)
    @Query(value = """
        UPDATE continue_reading_ranking_snapshot_meta
        SET snapshot_at = CONVERT_TZ(NOW(6), 'UTC', 'Asia/Seoul')
        WHERE id = :id
        """, nativeQuery = true)
    int updateSnapshotAt(@Param("id") long id);
}
