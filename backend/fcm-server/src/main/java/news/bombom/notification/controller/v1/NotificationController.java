package news.bombom.notification.controller.v1;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import news.bombom.notification.dto.request.NotificationSendRequest;
import news.bombom.notification.dto.request.NotificationSettingRequest;
import news.bombom.notification.dto.request.NotificationTokenRequest;
import news.bombom.notification.service.NotificationService;
import news.bombom.notification.service.NotificationTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationTokenService notificationTokenService;
    private final NotificationService notificationService;

    @PostMapping("/tokens")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerNotificationToken(@Valid @RequestBody NotificationTokenRequest request) {
        notificationTokenService.registerFcmToken(request.memberId(), request.deviceUuid(), request.token());
    }

    @PutMapping("/tokens")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void upsertNotificationToken(@Valid @RequestBody NotificationTokenRequest request) {
        notificationTokenService.upsertFcmToken(request.memberId(), request.deviceUuid(), request.token());
    }

    @PutMapping("/tokens/{memberId}/{deviceUuid}/settings")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateNotificationSettings(
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long memberId,
            @PathVariable @NotBlank String deviceUuid,
            @Valid @RequestBody NotificationSettingRequest request
    ) {
        notificationTokenService.updateNotificationSetting(memberId, deviceUuid, request.enabled());
    }

    @GetMapping("/tokens/{memberId}/{deviceUuid}/settings/status")
    public boolean getNotificationSettingsStatus(
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long memberId,
            @PathVariable @NotBlank String deviceUuid
    ) {
        return notificationTokenService.getNotificationEnabled(memberId, deviceUuid);
    }

    /**
     * FCM 알림 직접 발송
     */
    @PostMapping("/send")
    @ResponseStatus(HttpStatus.OK)
    public void sendNotification(@Valid @RequestBody NotificationSendRequest request) {
        notificationService.sendNotification(request.token(), request.title(), request.body(), request.articleId());
    }
}
