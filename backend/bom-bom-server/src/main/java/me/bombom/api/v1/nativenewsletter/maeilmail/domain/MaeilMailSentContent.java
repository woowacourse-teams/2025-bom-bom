package me.bombom.api.v1.nativenewsletter.maeilmail.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
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
        name = "uk_maeil_mail_sent_content",
        columnNames = {"member_id", "topic_id", "content_id"}
))
public class MaeilMailSentContent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long topicId;

    @Column(nullable = false)
    private Long contentId;

    @Column(nullable = false)
    private LocalDateTime sentAt;

    @Builder
    public MaeilMailSentContent(
            @NonNull Long memberId,
            @NonNull Long topicId,
            @NonNull Long contentId,
            @NonNull LocalDateTime sentAt
    ) {
        this.memberId = memberId;
        this.topicId = topicId;
        this.contentId = contentId;
        this.sentAt = sentAt;
    }
}
