package me.bombom.api.v1.newsletterrequest.domain;

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
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_newsletter_request_like_member_request",
                columnNames = {"member_id", "newsletter_request_id"}
        )
})
public class NewsletterRequestLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long newsletterRequestId;

    @Builder
    public NewsletterRequestLike(
            Long id,
            @NonNull Long memberId,
            @NonNull Long newsletterRequestId
    ) {
        this.id = id;
        this.memberId = memberId;
        this.newsletterRequestId = newsletterRequestId;
    }
}
