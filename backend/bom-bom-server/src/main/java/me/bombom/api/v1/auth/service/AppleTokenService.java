package me.bombom.api.v1.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.auth.dto.response.AppleTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppleTokenService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final Supplier<String> appleClientSecretSupplier;

    @Value("${oauth2.apple.client-id}")
    private String clientId;

    private static final String APPLE_TOKEN_URL = "https://appleid.apple.com/auth/token";
    private static final String APPLE_REVOKE_URL = "https://appleid.apple.com/auth/revoke";

    /**
     * Authorization Code로 Refresh Token 발급받기
     */
    public AppleTokenResponse getRefreshToken(String authorizationCode) {
        try {
            log.info("=== Apple Refresh Token 발급 시작 ===");
            log.info("authorizationCode: {}", authorizationCode);
            
            String clientSecret = appleClientSecretSupplier.get();
            log.info("clientSecret 길이: {}", clientSecret.length());

            // 요청 데이터 설정
            MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
            data.add("client_id", clientId);
            data.add("client_secret", clientSecret);
            data.add("code", authorizationCode);
            data.add("grant_type", "authorization_code");

            log.info("Apple Token 요청 URL: {}", APPLE_TOKEN_URL);
            log.info("요청 데이터: {}", data);

            // Apple 서버에 요청 (RestClient 사용)
            String responseBody = restClient.post()
                    .uri(APPLE_TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(data)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), (request, response) -> {
                        log.error("Apple Token 발급 4xx 에러: {}", response.getStatusText());
                        throw new RuntimeException("Apple Token 발급 실패: " + response.getStatusText());
                    })
                    .onStatus(status -> status.is5xxServerError(), (request, response) -> {
                        log.error("Apple Token 발급 5xx 에러: {}", response.getStatusText());
                        throw new RuntimeException("Apple Token 발급 실패: " + response.getStatusText());
                    })
                    .body(String.class);

            log.info("Apple Token 응답 본문: {}", responseBody);

            AppleTokenResponse tokenResponse = objectMapper.readValue(responseBody, AppleTokenResponse.class);
            log.info("=== Apple Refresh Token 발급 성공 ===");
            log.info("access_token 존재: {}", tokenResponse.getAccessToken() != null);
            log.info("refresh_token 존재: {}", tokenResponse.getRefreshToken() != null);
            log.info("id_token 존재: {}", tokenResponse.getIdToken() != null);
            return tokenResponse;

        } catch (Exception e) {
            log.error("Apple Refresh Token 발급 중 오류 발생", e);
            throw new RuntimeException("Apple Refresh Token 발급 실패", e);
        }
    }

    /**
     * Refresh Token으로 Access Token 갱신
     */
    public AppleTokenResponse refreshAccessToken(String refreshToken) {
        try {
            log.info("=== Apple Access Token 갱신 시작 ===");
            log.info("refreshToken: {}", refreshToken);

            String clientSecret = appleClientSecretSupplier.get();

            // 요청 데이터 설정
            MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
            data.add("client_id", clientId);
            data.add("client_secret", clientSecret);
            data.add("refresh_token", refreshToken);
            data.add("grant_type", "refresh_token");

            // Apple 서버에 요청 (RestClient 사용)
            String responseBody = restClient.post()
                    .uri(APPLE_TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(data)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), (request, response) -> {
                        log.error("Apple Token 갱신 4xx 에러: {}", response.getStatusText());
                        throw new RuntimeException("Apple Token 갱신 실패: " + response.getStatusText());
                    })
                    .onStatus(status -> status.is5xxServerError(), (request, response) -> {
                        log.error("Apple Token 갱신 5xx 에러: {}", response.getStatusText());
                        throw new RuntimeException("Apple Token 갱신 실패: " + response.getStatusText());
                    })
                    .body(String.class);

            log.info("Apple Token 갱신 응답 본문: {}", responseBody);

            AppleTokenResponse tokenResponse = objectMapper.readValue(responseBody, AppleTokenResponse.class);
            log.info("=== Apple Access Token 갱신 성공 ===");
            return tokenResponse;

        } catch (Exception e) {
            log.error("Apple Access Token 갱신 중 오류 발생", e);
            throw new RuntimeException("Apple Access Token 갱신 실패", e);
        }
    }

    /**
     * Refresh Token 철회
     */
    public void revokeToken(String refreshToken) {
        try {
            log.info("=== Apple Token 철회 시작 ===");
            log.info("refreshToken: {}", refreshToken);

            String clientSecret = appleClientSecretSupplier.get();

            // 요청 데이터 설정
            MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
            data.add("client_id", clientId);
            data.add("client_secret", clientSecret);
            data.add("token", refreshToken);
            data.add("token_type_hint", "refresh_token");

            log.info("Apple Token 철회 요청 URL: {}", APPLE_REVOKE_URL);
            log.info("요청 데이터: {}", data);

            // Apple 서버에 요청 (RestClient 사용)
            String responseBody = restClient.post()
                    .uri(APPLE_REVOKE_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(data)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), (request, response) -> {
                        log.error("Apple Token 철회 4xx 에러: {}", response.getStatusText());
                        throw new RuntimeException("Apple Token 철회 실패: " + response.getStatusText());
                    })
                    .onStatus(status -> status.is5xxServerError(), (request, response) -> {
                        log.error("Apple Token 철회 5xx 에러: {}", response.getStatusText());
                        throw new RuntimeException("Apple Token 철회 실패: " + response.getStatusText());
                    })
                    .body(String.class);

            log.info("Apple Token 철회 응답 본문: {}", responseBody);
            log.info("=== Apple Token 철회 성공 ===");

        } catch (Exception e) {
            log.error("Apple Token 철회 중 오류 발생", e);
            throw new RuntimeException("Apple Token 철회 실패", e);
        }
    }
}
