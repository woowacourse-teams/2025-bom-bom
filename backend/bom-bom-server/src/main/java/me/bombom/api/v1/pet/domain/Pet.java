package me.bombom.api.v1.pet.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long stageId;

    private int currentScore = 0;

    @Builder
    public Pet(
            Long id,
            @NotNull Long memberId,
            @NotNull Long stageId,
            int currentScore
    ) {
        this.id = id;
        this.memberId = memberId;
        this.stageId = stageId;
        this.currentScore = currentScore;
    }

    public void increaseCurrentScore(int score) {
        this.currentScore += score;
    }
}
