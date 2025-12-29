package me.bombom.api.v1.challenge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChallengeComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long articleId;

    @Column(nullable = false)
    private Long participantId;

    private String quotation;

    @Column(nullable = false)
    private String comment;

    @Builder
    public ChallengeComment(
            Long id,
            @NonNull Long articleId,
            @NonNull Long participantId,
            String quotation,
            @NonNull String comment
    ) {
        this.id = id;
        this.articleId = articleId;
        this.participantId = participantId;
        this.quotation = quotation;
        this.comment = comment;
    }
}
