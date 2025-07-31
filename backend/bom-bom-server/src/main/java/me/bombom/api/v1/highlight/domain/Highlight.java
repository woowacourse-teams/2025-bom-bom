package me.bombom.api.v1.highlight.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Highlight extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private HighlightLocation highlightLocation;

    private Long articleId;

    private String color;

    private String text;

    @Builder
    public Highlight(
            Long id,
            @NotNull HighlightLocation highlightLocation,
            @NotNull Long articleId,
            @NotNull String color,
            @NotNull String text
    ) {
        this.id = id;
        this.highlightLocation = highlightLocation;
        this.articleId = articleId;
        this.color = color;
        this.text = text;
    }

    public void changeColor(String color) {
        this.color = color;
    }
}
