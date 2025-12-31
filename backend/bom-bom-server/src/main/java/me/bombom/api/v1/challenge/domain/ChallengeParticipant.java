package me.bombom.api.v1.challenge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "challenge_participant", uniqueConstraints = {
        @UniqueConstraint(name = "uk_challenge_participant", columnNames = { "challenge_id", "member_id" })
})
public class ChallengeParticipant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long challengeId;

    @Column(nullable = false)
    private Long memberId;

    private Long challengeTeamId;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private int completedDays = 0;

    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private boolean isSurvived = true;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private int shield = 0;

    @Builder
    public ChallengeParticipant(
            Long id,
            @NonNull Long challengeId,
            @NonNull Long memberId,
            Long challengeTeamId,
            int completedDays,
            boolean isSurvived,
            int shield
    ) {
        this.id = id;
        this.challengeId = challengeId;
        this.memberId = memberId;
        this.challengeTeamId = challengeTeamId;
        this.completedDays = completedDays;
        this.isSurvived = isSurvived;
        this.shield = shield;
    }

    public int calculateProgress(int totalDays) {
        if (totalDays <= 0) {
            return 0;
        }
        int progress = (int) ((double) this.completedDays / totalDays * 100);
        return Math.min(progress, 100);
    }
}
