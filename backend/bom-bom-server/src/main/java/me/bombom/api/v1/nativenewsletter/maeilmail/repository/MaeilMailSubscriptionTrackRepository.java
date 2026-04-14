package me.bombom.api.v1.nativenewsletter.maeilmail.repository;

import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSubscriptionTrack;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaeilMailSubscriptionTrackRepository extends JpaRepository<MaeilMailSubscriptionTrack, Long> {
}
