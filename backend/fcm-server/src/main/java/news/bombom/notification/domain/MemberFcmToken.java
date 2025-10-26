package news.bombom.notification.domain;

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
import news.bombom.notification.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member_fcm_token",
       uniqueConstraints = @UniqueConstraint(columnNames = {"memberId", "deviceUuid"}))
public class MemberFcmToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private String deviceUuid;

    @Column(nullable = false, length = 300)
    private String fcmToken;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isNotificationEnabled = true;

    @Builder
    public MemberFcmToken(
            Long id,
            @NonNull Long memberId,
            @NonNull String deviceUuid,
            @NonNull String fcmToken,
            boolean isNotificationEnabled
    ) {
        this.id = id;
        this.memberId = memberId;
        this.deviceUuid = deviceUuid;
        this.fcmToken = fcmToken;
        this.isNotificationEnabled = isNotificationEnabled;
    }

    public void updateToken(String newToken) {
        this.fcmToken = newToken;
    }

    public void updateNotificationSetting(boolean enabled) {
        this.isNotificationEnabled = enabled;
    }
}
