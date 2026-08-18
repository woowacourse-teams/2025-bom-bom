package me.bombom.api.v1.auth.migration;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.auth.AppleClientSecretSupplier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppleUserMigrationRestClient implements AppleUserMigrationClient {

    private static final String APPLE_TOKEN_URI = "https://appleid.apple.com/auth/token";
    private static final String APPLE_USER_MIGRATION_URI = "https://appleid.apple.com/auth/usermigrationinfo";

    private final RestClient.Builder restClientBuilder;
    private final AppleClientSecretSupplier appleClientSecretSupplier;
    private final AppleUserMigrationProperties properties;

    @Override
    public String createTransferSub(String existingSub) {
        String clientSecret = appleClientSecretSupplier.generateFor(properties.clientId());
        String accessToken = requestMigrationAccessToken(clientSecret);
        Map<String, Object> response = restClientBuilder.build().post()
                .uri(APPLE_USER_MIGRATION_URI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("Authorization", "Bearer " + accessToken)
                .body(userMigrationRequestBody(existingSub, clientSecret))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        (request, res) -> {
                            String body = new String(res.getBody().readAllBytes());
                            log.error("Apple usermigrationinfo 호출 실패 - sub: {}, status: {}, body: {}",
                                    existingSub, res.getStatusCode(), body);
                            throw HttpClientErrorException.create(
                                    res.getStatusCode(), res.getStatusText(), res.getHeaders(), body.getBytes(), null);
                        })
                .body(new ParameterizedTypeReference<>() {});
        return requiredString(response, "transfer_sub");
    }

    private String requestMigrationAccessToken(String clientSecret) {
        Map<String, Object> response = restClientBuilder.build().post()
                .uri(APPLE_TOKEN_URI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(accessTokenRequestBody(clientSecret))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        return requiredString(response, "access_token");
    }

    private MultiValueMap<String, String> accessTokenRequestBody(String clientSecret) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("scope", "user.migration");
        body.add("client_id", properties.clientId());
        body.add("client_secret", clientSecret);
        return body;
    }

    private MultiValueMap<String, String> userMigrationRequestBody(String existingSub, String clientSecret) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("sub", existingSub);
        body.add("target", properties.targetTeamId());
        body.add("client_id", properties.clientId());
        body.add("client_secret", clientSecret);
        return body;
    }

    private String requiredString(Map<String, Object> response, String key) {
        if (response == null || !(response.get(key) instanceof String value) || !StringUtils.hasText(value)) {
            throw new IllegalStateException("Apple 사용자 이전 응답에 " + key + " 값이 없습니다.");
        }
        return value;
    }
}
