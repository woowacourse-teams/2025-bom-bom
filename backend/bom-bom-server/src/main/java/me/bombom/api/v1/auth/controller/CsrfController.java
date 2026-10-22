package me.bombom.api.v1.auth.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsrfController {

    private final boolean enabled;

    public CsrfController(@Value("${app.security.csrf-enabled:false}") boolean enabled) {
        this.enabled = enabled;
    }

    @GetMapping("/api/v1/auth/csrf")
    public ResponseEntity<CsrfResponse> csrf(CsrfToken csrfToken) {
        CsrfResponse body = enabled
                ? new CsrfResponse(true, csrfToken.getHeaderName(), csrfToken.getToken())
                : new CsrfResponse(false, null, null);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body);
    }

    public record CsrfResponse(boolean enabled, String headerName, String token) {
    }
}
