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
import lombok.NonNull;
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

    @Column(nullable = false, columnDefinition = "int default 0")
    private int currentScore = 0;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isAttended = false;

    @Builder
    public Pet(
            Long id,
            @NonNull Long memberId,
            @NonNull Long stageId,
            int currentScore,
            boolean isAttended
    ) {
        this.id = id;
        this.memberId = memberId;
        this.stageId = stageId;
        this.currentScore = currentScore;
        this.isAttended = isAttended;
    }

    public void increaseCurrentScore(int score) {
        this.currentScore += score;
    }

    public void markAsAttended() {
        this.isAttended = true;
    }

    public void updateStage(Stage newStage) {
        this.stageId = newStage.getId();
    }
}
