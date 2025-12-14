package me.bombom.api.v1.notice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "mediumtext")
    private String content;

    @Column(nullable = false)
    private Long noticeCategoryId;

    @Builder
    public Notice(
            Long id,
            @NonNull String title,
            @NonNull String content,
            @NonNull Long noticeCategoryId
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.noticeCategoryId = noticeCategoryId;
    }
}
