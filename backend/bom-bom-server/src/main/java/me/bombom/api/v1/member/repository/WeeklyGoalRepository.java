package me.bombom.api.v1.member.repository;

import java.util.Optional;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.domain.WeeklyGoal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeeklyGoalRepository extends JpaRepository<WeeklyGoal, Long> {

    Optional<WeeklyGoal> findByMemberId(Long memberId);
}
