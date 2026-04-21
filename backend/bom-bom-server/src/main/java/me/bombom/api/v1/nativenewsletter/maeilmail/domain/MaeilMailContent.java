package me.bombom.api.v1.nativenewsletter.maeilmail.domain;

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
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_maeil_mail_content_topic_order",
        columnNames = {"topic_id", "display_order"}
))
public class MaeilMailContent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long topicId;

    @Column(nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contentsText;

    @Column(nullable = false)
    private String contentsSummary;

    @Column(columnDefinition = "TINYINT", nullable = false)
    private int expectedReadTime;

    @Builder
    public MaeilMailContent(
            @NonNull Long topicId,
            int displayOrder,
            @NonNull String title,
            @NonNull String content,
            @NonNull String contentsText,
            @NonNull String contentsSummary,
            int expectedReadTime
    ) {
        this.topicId = topicId;
        this.displayOrder = displayOrder;
        this.title = title;
        this.content = content;
        this.contentsText = contentsText;
        this.contentsSummary = contentsSummary;
        this.expectedReadTime = expectedReadTime;
    }
}
