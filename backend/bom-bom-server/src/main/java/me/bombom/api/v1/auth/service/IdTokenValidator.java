package me.bombom.api.v1.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.exception.UnauthorizedException;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

/**
 * 네이티브 로그인용 id_token 서명/iss/aud 검증기
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdTokenValidator {

    private static final String APPLE_JWKS = "https://appleid.apple.com/auth/keys";
    private static final String APPLE_ISS = "https://appleid.apple.com";

    private static final String GOOGLE_JWKS = "https://www.googleapis.com/oauth2/v3/certs";
    private static final String GOOGLE_ISS = "https://accounts.google.com";

    private final JwtDecoder appleDecoder = buildDecoder(APPLE_JWKS, APPLE_ISS);
    private final JwtDecoder googleDecoder = buildDecoder(GOOGLE_JWKS, GOOGLE_ISS);

    public String validateAppleAndGetSubject(String idToken, String expectedAudience) {
        return validateAndGetSub(appleDecoder, idToken, expectedAudience, "apple");
    }

    public String validateGoogleAndGetSubject(String idToken, String expectedAudience) {
        return validateAndGetSub(googleDecoder, idToken, expectedAudience, "google");
    }

    private static JwtDecoder buildDecoder(String jwks, String issuer) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwks).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                new JwtIssuerValidator(issuer)
        ));
        return decoder;
    }

    private String validateAndGetSub(JwtDecoder decoder, String idToken, String expectedAudience, String provider) {
        try {
            Jwt jwt = decoder.decode(idToken);
            log.info("ID Token 검증 - provider: {}, actualAud: {}, expectedAud: {}", provider, jwt.getAudience(), expectedAudience);
            if (jwt.getAudience() == null || jwt.getAudience().isEmpty() || !jwt.getAudience().contains(expectedAudience)) {
                throw new UnauthorizedException(ErrorDetail.INVALID_TOKEN)
                        .addContext("provider", provider)
                        .addContext("reason", "invalid_audience")
                        .addContext("expectedAud", expectedAudience)
                        .addContext("actualAud", jwt.getAudience().toString());
            }
            return jwt.getSubject();
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            log.warn("id_token 검증 실패 - provider: {}, reason: {}", provider, e.getMessage());
            throw new UnauthorizedException(ErrorDetail.INVALID_TOKEN)
                    .addContext("provider", provider)
                    .addContext("reason", "invalid_id_token");
        }
    }
}


