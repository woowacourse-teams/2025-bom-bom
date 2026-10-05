package me.bombom.api.v1.newsletterrequest.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_newsletter_request_draft_request_id", columnNames = {"newsletter_request_id"})
})
public class NewsletterRequestDraft extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long newsletterRequestId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DraftCollectStatus collectStatus;

    @Column(nullable = false)
    private int collectAttemptCount;

    private LocalDateTime collectStartedAt;

    private String failureReason;

    private String name;

    private String description;

    @Column(length = 512)
    private String imageUrl;

    @Column(length = 60)
    private String email;

    private Long categoryId;

    @Column(length = 512)
    private String mainPageUrl;

    @Column(length = 512)
    private String subscribeUrl;

    private String issueCycle;

    @Column(length = 100)
    private String sender;

    @Column(length = 512)
    private String subscribeMethod;

    @Column(length = 512)
    private String previousNewsletterUrl;

    @Builder
    public NewsletterRequestDraft(
            Long id,
            @NonNull Long newsletterRequestId
    ) {
        this.id = id;
        this.newsletterRequestId = newsletterRequestId;
        this.collectStatus = DraftCollectStatus.PENDING;
        this.collectAttemptCount = 0;
    }

    public void resetToPending() {
        this.collectStatus = DraftCollectStatus.PENDING;
        this.collectAttemptCount = 0;
        this.collectStartedAt = null;
        this.failureReason = null;
        this.name = null;
        this.description = null;
        this.imageUrl = null;
        this.email = null;
        this.categoryId = null;
        this.mainPageUrl = null;
        this.subscribeUrl = null;
        this.issueCycle = null;
        this.sender = null;
        this.subscribeMethod = null;
        this.previousNewsletterUrl = null;
    }
}
