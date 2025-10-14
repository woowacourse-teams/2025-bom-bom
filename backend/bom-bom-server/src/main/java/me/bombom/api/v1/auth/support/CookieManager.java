package me.bombom.api.v1.auth.support;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

/**
 * 추후에 JWT로 변경 시를 대비해 미리 추가
 */
@Component
public class CookieManager {

    public void delete(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);

        response.addCookie(cookie);
        response.addHeader(
                "Set-Cookie",
                name + "=; Max-Age=0; Path=/; Secure; HttpOnly; SameSite=None"
        );
    }
}
