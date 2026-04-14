package me.bombom.api.v1.nativenewsletter.maeilmail.repository;

import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaeilMailSubscriptionRepository extends JpaRepository<MaeilMailSubscription, Long> {
}
