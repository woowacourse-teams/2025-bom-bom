package me.bombom.api.v1.newsletter.domain;

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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "newsletter_group_item", uniqueConstraints = {
        @UniqueConstraint(name = "uk_newsletter_group_item", columnNames = {"newsletter_group_id", "newsletter_id"})
})
public class NewsletterGroupItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long newsletterGroupId;

    @Column(nullable = false)
    private Long newsletterId;

    @Builder
    public NewsletterGroupItem(
            Long id,
            @NonNull Long newsletterGroupId,
            @NonNull Long newsletterId
    ) {
        this.id = id;
        this.newsletterGroupId = newsletterGroupId;
        this.newsletterId = newsletterId;
    }
}
