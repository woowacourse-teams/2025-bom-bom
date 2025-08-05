package me.bombom.api.v1.highlight.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
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

    @Column(nullable = false)
    private Long articleId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(nullable = false, name = "color", length = 10))
    private Color color;

    @Column(nullable = false, columnDefinition = "text")
    private String text;

    @Column(length = 500)
    private String memo;

    @Builder
    public Highlight(
            Long id,
            @NotNull HighlightLocation highlightLocation,
            @NotNull Long articleId,
            @NotNull Color color,
            @NotNull String text,
            String memo
    ) {
        this.id = id;
        this.highlightLocation = highlightLocation;
        this.articleId = articleId;
        this.color = color;
        this.text = text;
        this.memo = memo;
    }

    public void changeColor(Color color) {
        this.color = color;
    }

    public void editMemo(String memo) {
        this.memo = memo;
    }
}
