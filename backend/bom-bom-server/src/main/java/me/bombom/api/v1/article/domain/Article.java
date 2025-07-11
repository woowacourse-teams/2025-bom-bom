package me.bombom.api.v1.article.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Article extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 512)
    private String articleUrl;

    @Column(length = 512)
    private String thumbnailUrl;

    @Column(columnDefinition = "tinyint")
    private int expectedReadTime;

    private String contentsSummary;

    private boolean isRead;

    private Long memberId;

    private Long newsletterId;

    private LocalDateTime arrivedDateTime;
}
