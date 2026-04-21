package me.bombom.api.v1.nativenewsletter.maeilmail.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_maeil_mail_topic_progress",
        columnNames = {"member_id", "topic_id"}
))
public class MaeilMailTopicProgress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long topicId;

    @Column(nullable = false)
    private int completedCycles = 0;

    @Builder
    public MaeilMailTopicProgress(
            @NonNull Long memberId,
            @NonNull Long topicId
    ) {
        this.memberId = memberId;
        this.topicId = topicId;
        this.completedCycles = 0;
    }

    public void incrementCompletedCycles() {
        this.completedCycles++;
    }
}
