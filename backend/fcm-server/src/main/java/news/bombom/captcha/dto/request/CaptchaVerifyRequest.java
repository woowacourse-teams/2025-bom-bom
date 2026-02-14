package news.bombom.captcha.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CaptchaVerifyRequest(

        @NotBlank
        String gRecaptchaResponse
) {
}
