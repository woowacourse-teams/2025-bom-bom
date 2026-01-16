package me.bombom.api.v1.badge.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
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

    private Long challengeId;

    private String challengeName;

    @Builder
    public ChallengeBadge(
            Long id,
            @NonNull Long memberId,
            @NonNull BadgeType badgeType,
            @NonNull Long challengeId,
            @NonNull String challengeName
    ) {
        super(id, memberId, badgeType);
        this.challengeId = challengeId;
        this.challengeName = challengeName;
    }

    public static ChallengeBadge create(Long memberId, BadgeType badgeType, Long challengeId, String challengeName) {
        return ChallengeBadge.builder()
                .memberId(memberId)
                .badgeType(badgeType)
                .challengeId(challengeId)
                .challengeName(challengeName)
                .build();
    }
}
