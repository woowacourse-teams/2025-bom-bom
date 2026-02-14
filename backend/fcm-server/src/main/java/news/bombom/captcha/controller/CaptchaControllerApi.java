package news.bombom.captcha.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import news.bombom.captcha.dto.request.CaptchaVerifyRequest;
import news.bombom.captcha.dto.response.CaptchaVerifyResponse;

@Tag(name = "Captcha", description = "reCAPTCHA 검증 API")
public interface CaptchaControllerApi {

    @Operation(
            summary = "reCAPTCHA 검증",
            description = "reCAPTCHA v2 토큰을 검증합니다. 토큰은 2분 이내에만 유효합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "캡차 검증 완료 (성공 여부는 응답의 isSuccess 필드 참고)",
                    content = @Content(schema = @Schema(implementation = CaptchaVerifyResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (토큰 누락)"
            )
    })
    CaptchaVerifyResponse verify(CaptchaVerifyRequest request, HttpServletRequest httpRequest);
}
