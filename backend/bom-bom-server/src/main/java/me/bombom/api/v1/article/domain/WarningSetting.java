package me.bombom.api.v1.article.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WarningSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isVisible = true;

    @Builder
    public WarningSetting(
            Long id,
            @NonNull Long memberId,
            boolean isVisible) {
        this.memberId = memberId;
        this.isVisible = isVisible;
    }
}
