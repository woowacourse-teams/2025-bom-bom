package me.bombom.api.v1.article.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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
@Table(
    name = "search_recent",
    uniqueConstraints = @UniqueConstraint(name = "uk_article_id", columnNames = "article_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchRecent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long articleId;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long newsletterId;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "mediumtext")
    private String contents;

    @Column(nullable = false)
    private LocalDateTime arrivedDateTime;

    @Builder
    public SearchRecent(
            Long id,
            @NonNull Long articleId,
            @NonNull Long memberId,
            @NonNull Long newsletterId,
            @NonNull String title,
            @NonNull String contents,
            @NonNull LocalDateTime arrivedDateTime
    ) {
        this.id = id;
        this.articleId = articleId;
        this.memberId = memberId;
        this.newsletterId = newsletterId;
        this.title = title;
        this.contents = contents;
        this.arrivedDateTime = arrivedDateTime;
    }

    public static SearchRecent from(Article article) {
        return SearchRecent.builder()
                .articleId(article.getId())
                .memberId(article.getMemberId())
                .newsletterId(article.getNewsletterId())
                .title(article.getTitle())
                .contents(article.getContents())
                .arrivedDateTime(article.getArrivedDateTime())
                .build();
    }
}
