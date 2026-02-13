package news.bombom.notification.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import news.bombom.notification.domain.NotificationCategory;
import news.bombom.notification.dto.request.NotificationCategorySettingRequest;
import news.bombom.notification.dto.response.NotificationCategorySettingResponse;
import news.bombom.notification.service.NotificationSettingService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationSettingController {

    private final NotificationSettingService notificationSettingService;

    @GetMapping("/{memberId}/settings")
    public List<NotificationCategorySettingResponse> getCategorySettings(
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long memberId
    ) {
        return notificationSettingService.getCategorySettings(memberId);
    }

    @GetMapping("/{memberId}/settings/{category}")
    public NotificationCategorySettingResponse getCategorySetting(
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long memberId,
            @PathVariable NotificationCategory category
    ) {
        return notificationSettingService.getCategorySetting(memberId, category);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/{memberId}/settings/{category}")
    public void updateNotificationCategorySetting(
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long memberId,
            @PathVariable NotificationCategory category,
            @Valid @RequestBody NotificationCategorySettingRequest request
    ) {
        notificationSettingService.updateCategorySetting(memberId, category, request.enabled());
    }
}
