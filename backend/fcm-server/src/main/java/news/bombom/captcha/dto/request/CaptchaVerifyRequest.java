package news.bombom.captcha.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CaptchaVerifyRequest(

        @NotBlank
        String gRecaptchaResponse
) {
}
