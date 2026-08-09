package me.bombom.api.v1.member.domain;

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
@Table(
        name = "member_notification_setting",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"member_id", "category"})}
)
public class MemberNotificationSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private boolean isEnabled;

    @Builder
    public MemberNotificationSetting(
            Long id,
            @NonNull Long memberId,
            @NonNull String category,
            boolean isEnabled
    ) {
        this.id = id;
        this.memberId = memberId;
        this.category = category;
        this.isEnabled = isEnabled;
    }
}
