package me.bombom.api.v1.badge.repository;

import me.bombom.api.v1.badge.domain.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
}
