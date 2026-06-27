package news.bombomemail.newsletter.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import news.bombomemail.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Newsletter extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, length = 512)
    private String imageUrl;

    @Column(nullable = false, length = 60)
    private String email;

    @Column(nullable = false)
    private Long categoryId;

    @Column(nullable = false)
    private Long detailId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NewsletterSource source = NewsletterSource.EXTERNAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NewsletterPublicationStatus status = NewsletterPublicationStatus.ACTIVE;

    @Builder
    public Newsletter(
            Long id,
            @NonNull String name,
            @NonNull String description,
            @NonNull String imageUrl,
            @NonNull String email,
            @NonNull Long categoryId,
            @NonNull Long detailId,
            NewsletterSource source,
            NewsletterPublicationStatus status
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.email = email;
        this.categoryId = categoryId;
        this.detailId = detailId;
        this.source = source == null ? NewsletterSource.EXTERNAL : source;
        this.status = status == null ? NewsletterPublicationStatus.ACTIVE : status;
    }
}
