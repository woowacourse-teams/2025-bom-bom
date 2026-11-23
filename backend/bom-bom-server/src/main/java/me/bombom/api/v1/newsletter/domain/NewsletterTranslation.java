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
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_newsletter_translation_newsletter_language",
        columnNames = {"newsletter_id", "language"}
))
public class NewsletterTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long newsletterId;

    @Column(nullable = false)
    private String language;

    private String name;

    private String description;

    @Builder
    public NewsletterTranslation(
            @NonNull Long newsletterId,
            @NonNull String language,
            String name,
            String description
    ) {
        this.newsletterId = newsletterId;
        this.language = language;
        this.name = name;
        this.description = description;
    }
}
