package news.bombom.captcha.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombom.captcha.dto.request.CaptchaVerifyRequest;
import news.bombom.captcha.dto.response.CaptchaVerifyResponse;
import news.bombom.captcha.service.CaptchaService;
import news.bombom.captcha.util.ClientIpResolver;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications/capcha")
public class CaptchaController implements CaptchaControllerApi {

    private final CaptchaService captchaService;

    @Override
    @PostMapping
    public CaptchaVerifyResponse verify(@Valid @RequestBody CaptchaVerifyRequest request, HttpServletRequest httpRequest) {
        String clientIp = ClientIpResolver.getClientIp(httpRequest);
        return captchaService.verify(request.gRecaptchaResponse(), clientIp);
    }
}
