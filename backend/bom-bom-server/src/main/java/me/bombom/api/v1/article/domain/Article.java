package me.bombom.api.v1.article.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import java.time.LocalDate;
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
public class Article extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "mediumtext")
    private String contents;

    @Column(length = 512)
    private String thumbnailUrl;

    @Column(columnDefinition = "tinyint")
    private int expectedReadTime;

    @Column(nullable = false)
    private String contentsSummary;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isRead = false;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long newsletterId;

    @Column(nullable = false)
    private LocalDateTime arrivedDateTime;

    @Builder
    public Article(
            Long id,
            @NonNull String title,
            @NonNull String contents,
            String thumbnailUrl,
            int expectedReadTime,
            @NonNull String contentsSummary,
            boolean isRead,
            @NonNull Long memberId,
            @NonNull Long newsletterId,
            @NonNull LocalDateTime arrivedDateTime
    ) {
        this.id = id;
        this.title = title;
        this.contents = contents;
        this.thumbnailUrl = thumbnailUrl;
        this.expectedReadTime = expectedReadTime;
        this.contentsSummary = contentsSummary;
        this.isRead = isRead;
        this.memberId = memberId;
        this.newsletterId = newsletterId;
        this.arrivedDateTime = arrivedDateTime;
    }

    public void markAsRead() {
        isRead = true;
    }

    public boolean isArrivedToday() {
        return arrivedDateTime.toLocalDate().isEqual(LocalDate.now());
    }

    public boolean isOwner(Long memberId) {
        return this.memberId.equals(memberId);
    }
}
