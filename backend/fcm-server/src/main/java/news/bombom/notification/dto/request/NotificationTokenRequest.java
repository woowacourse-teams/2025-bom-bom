package news.bombom.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record NotificationTokenRequest(

        @NotNull
        @Positive(message = "id는 1 이상의 값이어야 합니다.")
        Long memberId,

        @NotBlank
        String deviceUuid,

        @NotBlank
        String token
) {}
