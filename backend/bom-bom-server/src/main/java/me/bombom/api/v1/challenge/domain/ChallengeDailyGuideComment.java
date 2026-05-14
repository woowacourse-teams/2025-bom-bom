package me.bombom.api.v1.challenge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.bombom.api.v1.common.BaseEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChallengeDailyGuideComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long guideId;

    @Column(nullable = false)
    private Long participantId;

    @Column(nullable = false, length = 1000)
    private String content;

    @Builder
    public ChallengeDailyGuideComment(Long guideId, Long participantId, String content) {
        this.guideId = guideId;
        this.participantId = participantId;
        this.content = content;
    }
}
