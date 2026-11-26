package news.bombom.notification.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import news.bombom.notification.domain.DevicePlatform;
import news.bombom.notification.domain.NotificationCategory;

public record NotificationOpenRequest(

        NotificationCategory category,

        @Positive
        Long notificationId,

        @Positive
        Long scheduleId,

        @Positive
        Long memberId,

        @Size(max = 255)
        String deviceUuid,

        @NotNull
        DevicePlatform platform,

        @Size(max = 50)
        String appVersion
) {

    /**
     * 알림 row id는 카테고리 테이블마다 따로 증가하므로 notificationId는 회원별 발송 카테고리와 함께 와야 한다.
     */
    @AssertTrue(message = "notificationId는 EVENT가 아닌 category와 함께 보내야 합니다.")
    public boolean isNotificationIdWithMemberCategory() {
        return notificationId == null || (category != null && !category.isUseTopic());
    }

    @AssertTrue(message = "scheduleId는 EVENT category와 함께 보내야 합니다.")
    public boolean isScheduleIdWithTopicCategory() {
        return scheduleId == null || (category != null && category.isUseTopic());
    }
}
