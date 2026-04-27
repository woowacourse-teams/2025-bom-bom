package me.bombom.api.v1.nativenewsletter.maeilmail.domain;

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
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MaeilMailUserAnswer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long contentId;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Builder
    public MaeilMailUserAnswer(
            @NonNull Long contentId,
            @NonNull Long memberId,
            @NonNull String answer
    ) {
        this.contentId = contentId;
        this.memberId = memberId;
        this.answer = answer;
    }
}
