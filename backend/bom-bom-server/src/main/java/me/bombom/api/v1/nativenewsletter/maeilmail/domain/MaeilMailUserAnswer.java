package me.bombom.api.v1.nativenewsletter.maeilmail.domain;

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
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_maeil_mail_user_answer_member_issue_history",
        columnNames = {"member_id", "issue_history_id"}
))
public class MaeilMailUserAnswer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long issueHistoryId;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Builder
    public MaeilMailUserAnswer(
            @NonNull Long issueHistoryId,
            @NonNull Long memberId,
            @NonNull String answer
    ) {
        this.issueHistoryId = issueHistoryId;
        this.memberId = memberId;
        this.answer = answer;
    }
}
