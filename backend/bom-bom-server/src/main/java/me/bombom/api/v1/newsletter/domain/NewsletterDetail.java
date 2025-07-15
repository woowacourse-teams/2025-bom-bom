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

    @Column(length=512, nullable = false)
    private String mainPageUrl;

    @Column(length=512, nullable = false)
    private String subscribeUrl;

    @Column(nullable = false)
    private String issueCycle;

    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private int subscribeCount;

    @Builder
    public NewsletterDetail(
            Long id,
            @NonNull String mainPageUrl,
            @NonNull String subscribeUrl,
            @NonNull String issueCycle,
            int subscribeCount
    ) {
        this.id = id;
        this.mainPageUrl = mainPageUrl;
        this.subscribeUrl = subscribeUrl;
        this.issueCycle = issueCycle;
        this.subscribeCount = subscribeCount;
    }
}
