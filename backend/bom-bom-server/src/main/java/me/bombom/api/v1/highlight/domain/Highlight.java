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
import lombok.NonNull;
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
    private Long memberId;

    @Column(nullable = false)
    private Long articleId;

    @Column(nullable = false)
    private Long newsletterId;

    /**
     * @Column 변경 시, Color 내부 주석에도 변경 필요
     */
    @Embedded
    @AttributeOverride(name = "value", column = @Column(nullable = false, name = "color", length = 10))
    private Color color;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String text;

    @Column(length = 500)
    private String memo;

    @Builder
    public Highlight(
            Long id,
            @NonNull HighlightLocation highlightLocation,
            @NonNull Long memberId,
            @NonNull Long articleId,
            @NonNull Long newsletterId,
            @NonNull Color color,
            @NonNull String title,
            @NonNull String text,
            String memo
    ) {
        this.id = id;
        this.highlightLocation = highlightLocation;
        this.memberId = memberId;
        this.articleId = articleId;
        this.newsletterId = newsletterId;
        this.color = color;
        this.title = title;
        this.text = text;
        this.memo = memo;
    }

    public void changeColor(Color color) {
        this.color = color;
    }

    public void editMemo(String memo) {
        this.memo = memo;
    }

    public boolean isNotOwner(Long memberId) {
        return !this.memberId.equals(memberId);
    }
}
