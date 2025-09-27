package me.bombom.api.v1.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AppleCallbackController {

    private static final String APPLE_SECURITY_CALLBACK = "/login/oauth2/code/apple";
    private static final String SESSION_ATTR_USER_JSON = "appleUserParam";

    /**
     * userJson
     *{
     *   "name": {
     *     "firstName": "string",
     *     "lastName": "string"
     *   },
     *   "email": "string"
     * }
     */
    @PostMapping("/login/sso/verify/success/apple")
    public void handleAppleFormPost(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "user", required = false) String userJson
    ) throws Exception {
        if (userJson != null && !userJson.isBlank()) {
            HttpSession session = request.getSession(true);
            session.setAttribute(SESSION_ATTR_USER_JSON, userJson);
            log.info("Apple 콜백 user 캡처 완료(컨트롤러) — 세션 저장");
        } else {
            log.info("Apple 콜백에 user 파라미터가 없음");
        }
        // Spring Security 표준 콜백으로 내부 forward (폼 파라미터 유지)
        request.getRequestDispatcher(APPLE_SECURITY_CALLBACK).forward(request, response);
    }
}
