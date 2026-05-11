package me.bombom.api.v1.nativenewsletter.maeilmail.repository;

import java.util.List;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSubscriptionTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MaeilMailSubscriptionTrackRepository extends JpaRepository<MaeilMailSubscriptionTrack, Long> {

    List<MaeilMailSubscriptionTrack> findByMemberId(Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM MaeilMailSubscriptionTrack mt
            WHERE mt.memberId = :memberId
    """)
    void deleteByMemberId(Long memberId);
}
