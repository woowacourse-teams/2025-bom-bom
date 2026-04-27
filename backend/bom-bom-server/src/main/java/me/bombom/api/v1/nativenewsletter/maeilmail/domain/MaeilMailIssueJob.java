package me.bombom.api.v1.nativenewsletter.maeilmail.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_maeil_mail_issue_job_issue_date",
        columnNames = "issue_date"
))
public class MaeilMailIssueJob extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MaeilMailIssueJobStatus status;

    @Column(nullable = false)
    private Long lastProcessedTrackId;

    @Column(nullable = false)
    private long chunkCount;

    @Column(nullable = false)
    private long processedTrackCount;

    @Column(nullable = false)
    private long issuedArticleCount;

    @Column(nullable = false)
    private long previouslyIssuedTrackCount;

    @Column(length = 1000)
    private String failedMessage;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private LocalDateTime failedAt;

    private MaeilMailIssueJob(
            @NonNull LocalDate issueDate,
            @NonNull Long startTrackId,
            @NonNull LocalDateTime startedAt
    ) {
        this.issueDate = issueDate;
        this.status = MaeilMailIssueJobStatus.RUNNING;
        this.lastProcessedTrackId = startTrackId;
        this.startedAt = startedAt;
    }

    public static MaeilMailIssueJob start(
            LocalDate issueDate,
            Long startTrackId,
            LocalDateTime startedAt
    ) {
        return new MaeilMailIssueJob(issueDate, startTrackId, startedAt);
    }

    public boolean isCompleted() {
        return status == MaeilMailIssueJobStatus.COMPLETED;
    }

    public void resume(LocalDateTime startedAt) {
        if (isCompleted()) {
            return;
        }

        this.status = MaeilMailIssueJobStatus.RUNNING;
        this.failedMessage = null;
        this.failedAt = null;
        if (this.startedAt == null) {
            this.startedAt = startedAt;
        }
    }

    public void complete(LocalDateTime completedAt) {
        this.status = MaeilMailIssueJobStatus.COMPLETED;
        this.completedAt = completedAt;
        this.failedMessage = null;
        this.failedAt = null;
    }

    public void fail(String failedMessage, LocalDateTime failedAt) {
        if (isCompleted()) {
            return;
        }

        this.status = MaeilMailIssueJobStatus.FAILED;
        this.failedMessage = failedMessage;
        this.failedAt = failedAt;
    }
}
