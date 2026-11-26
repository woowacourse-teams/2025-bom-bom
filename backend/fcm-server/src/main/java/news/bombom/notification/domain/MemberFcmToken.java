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
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "device_uuid"}))
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

    /**
     * 기기 OS의 알림 권한 허용 여부. null이면 앱이 아직 보고하지 않음(구버전 앱)
     */
    private Boolean osPermissionGranted;

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

    public void updateOsPermission(boolean granted) {
        this.osPermissionGranted = granted;
    }
}
