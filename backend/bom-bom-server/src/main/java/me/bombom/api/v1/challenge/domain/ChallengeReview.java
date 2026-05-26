package me.bombom.api.v1.challenge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_challenge_review_challenge_id_member_id",
        columnNames = {"challenge_id", "member_id"}
))
public class ChallengeReview extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long challengeId;

    @Column(nullable = false)
    private Long memberId;

    @NotBlank
    @Column(nullable = false, length = 2000)
    private String comment;

    private boolean isPrivate;

    @Builder
    public ChallengeReview(
            Long id,
            @NonNull Long memberId,
            @NonNull Long challengeId,
            @NotBlank String comment,
            boolean isPrivate
    ) {
        this.id = id;
        this.memberId = memberId;
        this.challengeId = challengeId;
        this.comment = comment;
        this.isPrivate = isPrivate;
    }

    public boolean isOwnedBy(Long memberId) {
        return this.memberId.equals(memberId);
    }

    public void update(String comment, boolean isPrivate) {
        this.comment = comment;
        this.isPrivate = isPrivate;
    }
}
