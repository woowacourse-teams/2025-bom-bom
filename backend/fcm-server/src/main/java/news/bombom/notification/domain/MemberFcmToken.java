package news.bombom.fcm.domain;

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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member_fcm_token",
       uniqueConstraints = @UniqueConstraint(columnNames = {"memberId", "deviceUuid"}))
public class MemberFcmToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private String deviceUuid;

    @Column(nullable = false, length = 300)
    private String fcmToken;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isNotificationEnabled = true;

    @Builder
    public MemberFcmToken(
            Long id,
            @NonNull Long memberId,
            @NonNull String deviceUuid,
            @NonNull String fcmToken,
            @NonNull LocalDateTime createdAt,
            @NonNull LocalDateTime updatedAt,
            boolean isNotificationEnabled
    ) {
        this.id = id;
        this.memberId = memberId;
        this.deviceUuid = deviceUuid;
        this.fcmToken = fcmToken;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isNotificationEnabled = isNotificationEnabled;
    }

    public void updateToken(String newToken) {
        this.fcmToken = newToken;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateNotificationSetting(boolean enabled) {
        this.isNotificationEnabled = enabled;
        this.updatedAt = LocalDateTime.now();
    }
}
