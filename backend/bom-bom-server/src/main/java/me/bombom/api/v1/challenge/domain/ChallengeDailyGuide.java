package me.bombom.api.v1.challenge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class ChallengeDailyGuide extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    private Long challengeId;

    @Column(nullable = false)
    private int dayIndex;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DailyGuideType type;

    @Column(nullable = false, length = 2048)
    private String imageUrl;

    @Column(length = 1000)
    private String notice;

    @Column(nullable = false)
    private boolean commentEnabled;

    @Builder
    public ChallengeDailyGuide(@NonNull Long challengeId,
                               int dayIndex,
                               @NonNull DailyGuideType type,
                               @NonNull String imageUrl,
                               String notice,
                               boolean commentEnabled) {
        this.challengeId = challengeId;
        this.dayIndex = dayIndex;
        this.type = type;
        this.imageUrl = imageUrl;
        this.notice = notice;
        this.commentEnabled = commentEnabled;
    }
}
