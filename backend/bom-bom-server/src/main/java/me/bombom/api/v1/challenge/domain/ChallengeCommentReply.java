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
public class ChallengeCommentReply extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long participantId;

    @Column(nullable = false)
    private Long commentId;

    @Column(nullable = false, length = 500)
    private String reply;

    @Builder
    public ChallengeCommentReply(
            @NonNull Long participantId,
            @NonNull Long commentId,
            @NonNull String reply
    ) {
        this.participantId = participantId;
        this.commentId = commentId;
        this.reply = reply;
    }
}
