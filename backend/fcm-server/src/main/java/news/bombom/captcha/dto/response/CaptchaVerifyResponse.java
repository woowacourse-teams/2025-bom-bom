package news.bombom.captcha.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

public record CaptchaVerifyResponse(

        @Schema(description = "검증 성공 여부", requiredMode = RequiredMode.REQUIRED)
        boolean isSuccess,
        
        @Schema(description = "메시지", requiredMode = RequiredMode.REQUIRED)
        String message
) {

    public static CaptchaVerifyResponse success() {
        return new CaptchaVerifyResponse(true, "캡차 검증 성공");
    }

    public static CaptchaVerifyResponse fail(String message) {
        return new CaptchaVerifyResponse(false, message);
    }
}
