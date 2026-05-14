package me.bombom.api.v1.reading.repository;

import me.bombom.api.v1.reading.domain.ReadingSnapshotMeta;
import me.bombom.api.v1.reading.domain.ReadingSnapshotType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReadingSnapshotMetaRepository extends JpaRepository<ReadingSnapshotMeta, ReadingSnapshotType> {

    @Modifying(clearAutomatically = true)
    @Query(value = """
        UPDATE reading_snapshot_meta
        SET snapshot_at = CONVERT_TZ(NOW(6), 'UTC', 'Asia/Seoul')
        WHERE snapshot_type = :snapshotType
        """, nativeQuery = true)
    int updateSnapshotAt(@Param("snapshotType") String snapshotType);
}
