package me.bombom.api.v1.article.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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
public class PreviousArticle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "mediumtext")
    private String contents;

    //TODO: 이거 아티클 받을 때 계산되어서 들어오는 값인데 직접 계산할지, 아니면 아예 없앨지
    @Column(columnDefinition = "tinyint")
    private int expectedReadTime;

    @Column(nullable = false)
    private String contentsSummary;

    @Column(nullable = false)
    private Long newsletterId;

    @Column(nullable = false)
    private LocalDateTime arrivedDateTime;

    @Builder
    public PreviousArticle (
            Long id,
            @NonNull String title,
            @NonNull String contents,
            int expectedReadTime,
            @NonNull String contentsSummary,
            @NonNull Long newsletterId,
            @NonNull LocalDateTime arrivedDateTime
    ) {
        this.id = id;
        this.title = title;
        this.contents = contents;
        this.expectedReadTime = expectedReadTime;
        this.contentsSummary = contentsSummary;
        this.newsletterId = newsletterId;
        this.arrivedDateTime = arrivedDateTime;
    }
}
