package me.bombom.api.v1.nativenewsletter.maeilmail.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_maeil_mail_issue_history_date_member_topic",
        columnNames = {"issue_date", "member_id", "topic_id"}
))
public class MaeilMailIssueHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long topicId;

    @Builder
    public MaeilMailIssueHistory(
            @NonNull LocalDate issueDate,
            @NonNull Long memberId,
            @NonNull Long topicId
    ) {
        this.issueDate = issueDate;
        this.memberId = memberId;
        this.topicId = topicId;
    }
}
