package me.bombom.api.v1.newsletter.domain;

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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NewsletterDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length=512)
    private String mainPageUrl;

    @Column(nullable = false, length=512)
    private String subscribeUrl;

    @Column(nullable = false)
    private String issueCycle;

    @Column(nullable = false, columnDefinition = "BIGINT")
    private int subscribeCount;

    @Column(nullable = false, length = 100)
    private String sender;

    @Column(length=512)
    private String previousNewsletterUrl;

    @Column(nullable = false)
    private boolean previousAllowed;

    @Column(length=512)
    private String subscribeMethod;

    @Builder
    public NewsletterDetail(
            Long id,
            @NonNull String mainPageUrl,
            @NonNull String subscribeUrl,
            @NonNull String issueCycle,
            int subscribeCount,
            @NonNull String sender,
            String previousNewsletterUrl,
            boolean previousAllowed,
            String subscribeMethod
    ) {
        this.id = id;
        this.mainPageUrl = mainPageUrl;
        this.subscribeUrl = subscribeUrl;
        this.issueCycle = issueCycle;
        this.subscribeCount = subscribeCount;
        this.sender = sender;
        this.previousNewsletterUrl = previousNewsletterUrl;
        this.previousAllowed = previousAllowed;
        this.subscribeMethod = subscribeMethod;
    }
}
