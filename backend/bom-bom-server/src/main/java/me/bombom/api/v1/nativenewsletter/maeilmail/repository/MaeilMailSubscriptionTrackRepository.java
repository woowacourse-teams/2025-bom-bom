package me.bombom.api.v1.nativenewsletter.maeilmail.repository;

import java.util.List;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSubscriptionTrack;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaeilMailSubscriptionTrackRepository extends JpaRepository<MaeilMailSubscriptionTrack, Long> {

    List<MaeilMailSubscriptionTrack> findByMemberId(Long memberId);

    void deleteByMemberId(Long memberId);
}
