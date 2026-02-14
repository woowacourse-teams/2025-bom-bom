package news.bombom.notification.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import news.bombom.notification.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_member_category", columnNames = {"memberId", "category"})
})
public class MemberNotificationSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationCategory category;

    @Column(nullable = false)
    private boolean isEnabled;

    @Builder
    public MemberNotificationSetting(
            @NonNull Long memberId,
            @NonNull NotificationCategory category,
            boolean isEnabled
    ) {
        this.memberId = memberId;
        this.category = category;
        this.isEnabled = isEnabled;
    }

    public void updateEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
}
