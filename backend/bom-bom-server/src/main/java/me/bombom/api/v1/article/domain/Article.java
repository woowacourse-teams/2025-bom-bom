package me.bombom.api.v1.article.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 512, nullable = false)
    private String articleUrl;

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
}
