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

    @Column(nullable = false)
    private int completedDays;

    @Column(nullable = false)
    private boolean isSurvived;

    @Column(nullable = false)
    private int shield;

    @Builder
    public ChallengeParticipant(
            Long id,
            @NonNull Long challengeId,
            @NonNull Long memberId,
            Long challengeTeamId,
            int completedDays,
            Boolean isSurvived,
            int shield) {
        this.id = id;
        this.challengeId = challengeId;
        this.memberId = memberId;
        this.challengeTeamId = challengeTeamId;
        this.completedDays = completedDays;
        this.isSurvived = isSurvived != null ? isSurvived : true;
        this.shield = shield;
    }
}
