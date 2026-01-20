package me.bombom.api.v1.badge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Getter
@DiscriminatorValue("CHALLENGE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChallengeBadge extends Badge {

    @Enumerated(EnumType.STRING)
    @Column(name = "badge_grade", length = 20)
    private BadgeGrade grade;

    private Long challengeId;

    private String challengeName;

    private Integer challengeGeneration;

    @Builder
    public ChallengeBadge(
            Long id,
            @NonNull Long memberId,
            @NonNull BadgeGrade grade,
            @NonNull Long challengeId,
            @NonNull String challengeName,
            @NonNull Integer challengeGeneration
    ) {
        super(id, memberId, BadgeCategory.CHALLENGE);
        this.grade = grade;
        this.challengeId = challengeId;
        this.challengeName = challengeName;
        this.challengeGeneration = challengeGeneration;
    }

    public static ChallengeBadge create(
            Long memberId,
            BadgeGrade grade,
            Long challengeId,
            String challengeName,
            Integer challengeGeneration
    ) {
        return ChallengeBadge.builder()
                .memberId(memberId)
                .grade(grade)
                .challengeId(challengeId)
                .challengeName(challengeName)
                .challengeGeneration(challengeGeneration)
                .build();
    }
}
