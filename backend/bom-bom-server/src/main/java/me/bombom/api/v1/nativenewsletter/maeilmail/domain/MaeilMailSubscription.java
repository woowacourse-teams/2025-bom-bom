package me.bombom.api.v1.nativenewsletter.maeilmail.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_maeil_mail_subscription_subscribe_id", columnNames = {"subscribe_id"})
})
public class MaeilMailSubscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long subscribeId;

    @Column(nullable = false)
    private Long memberId;

    @Convert(converter = WeeklyIssueCountConverter.class)
    @Column(nullable = false, columnDefinition = "TINYINT")
    private WeeklyIssueCount weeklyIssueCount;

    @Builder
    public MaeilMailSubscription(Long subscribeId, Long memberId, WeeklyIssueCount weeklyIssueCount) {
        this.subscribeId = subscribeId;
        this.memberId = memberId;
        this.weeklyIssueCount = weeklyIssueCount;
    }
}
