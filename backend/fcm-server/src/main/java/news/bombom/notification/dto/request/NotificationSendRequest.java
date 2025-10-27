package news.bombom.notification.dto.request;

import jakarta.validation.constraints.NotBlank;

public record NotificationSendRequest(

        @NotBlank(message = "FCM 토큰은 필수입니다.")
        String token,

        @NotBlank(message = "알림 제목은 필수입니다.")
        String title,

        @NotBlank(message = "알림 내용은 필수입니다.")
        String body,

        String articleId
) {}
