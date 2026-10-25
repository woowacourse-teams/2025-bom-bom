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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.bombom.api.v1.common.BaseEntity;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_newsletter_request_normalized_url", columnNames = {"normalized_url"})
})
public class NewsletterRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String requestedName;

    @Column(nullable = false, length = 512)
    private String requestedUrl;

    @Column(nullable = false, length = 512)
    private String normalizedUrl;

    @Column(nullable = false)
    private Long requesterMemberId;

    @Column(length = 200)
    private String reason;

    @Column(nullable = false)
    private boolean isNotificationEnabled = true;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NewsletterRequestStatus status;

    @Column(nullable = false)
    private int likeCount;

    private Long newsletterId;

    private String rejectReason;

    @Builder
    public NewsletterRequest(
            Long id,
            @NonNull String requestedName,
            @NonNull String requestedUrl,
            @NonNull String normalizedUrl,
            @NonNull Long requesterMemberId,
            String reason,
            boolean isNotificationEnabled
    ) {
        this.id = id;
        this.requestedName = requestedName;
        this.requestedUrl = requestedUrl;
        this.normalizedUrl = normalizedUrl;
        this.requesterMemberId = requesterMemberId;
        this.reason = reason;
        this.isNotificationEnabled = isNotificationEnabled;
        this.status = NewsletterRequestStatus.RECEIVED;
        this.likeCount = 0;
    }

    public boolean isInProgress() {
        return status.isInProgress();
    }

    public boolean isRequestedBy(Long memberId) {
        return requesterMemberId.equals(memberId);
    }

    public void reopen(
            @NonNull String requestedName,
            @NonNull String requestedUrl,
            @NonNull Long requesterMemberId,
            String reason,
            boolean isNotificationEnabled
    ) {
        if (status != NewsletterRequestStatus.REJECTED) {
            throw new CIllegalArgumentException(ErrorDetail.DUPLICATED_DATA)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "newsletterRequest")
                    .addContext("newsletterRequestId", id)
                    .addContext("status", status);
        }
        this.requestedName = requestedName;
        this.requestedUrl = requestedUrl;
        this.requesterMemberId = requesterMemberId;
        this.reason = reason;
        this.isNotificationEnabled = isNotificationEnabled;
        this.status = NewsletterRequestStatus.RECEIVED;
        this.likeCount = 0;
        this.rejectReason = null;
    }
}
