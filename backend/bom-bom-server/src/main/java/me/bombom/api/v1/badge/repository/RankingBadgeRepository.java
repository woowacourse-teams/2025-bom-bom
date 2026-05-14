package me.bombom.api.v1.badge.repository;

import me.bombom.api.v1.badge.domain.RankingBadge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RankingBadgeRepository extends JpaRepository<RankingBadge, Long> {

    boolean existsByMemberIdAndPeriodYearAndPeriodMonth(Long memberId, Integer periodYear, Integer periodMonth);
}
