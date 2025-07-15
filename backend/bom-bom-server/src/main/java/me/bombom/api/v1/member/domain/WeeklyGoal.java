package me.bombom.api.v1.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.bombom.api.v1.common.BaseEntity;
import org.hibernate.validator.constraints.UniqueElements;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyGoal extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @UniqueElements
    @Column(unique = true, nullable = false)
    private Long memberId;

    @Column(columnDefinition = "TINYINT", nullable = false)
    private int weeklyGoalCount;

    @Column(columnDefinition = "TINYINT DEFAULT 0", nullable = false)
    private int currentCount;
}
