package me.bombom.api.v1.auth.migration;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.auth.AppleClientSecretSupplier;
import me.bombom.api.v1.auth.ApplePrivateKeyLoader;
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
public class AppleUserExchangeRestClient implements AppleUserExchangeClient {

    private static final String APPLE_TOKEN_URI = "https://appleid.apple.com/auth/token";
    private static final String APPLE_USER_MIGRATION_URI = "https://appleid.apple.com/auth/usermigrationinfo";

    private final RestClient.Builder restClientBuilder;
    private final AppleUserExchangeProperties properties;

    public AppleUserExchangeRestClient(RestClient.Builder restClientBuilder, AppleUserExchangeProperties properties) {
        this.restClientBuilder = restClientBuilder;
        this.properties = properties;
    }

    @Override
    public String exchangeSub(String transferSub) {
        String clientSecret = appleClientSecretSupplier().get();
        String accessToken = requestExchangeAccessToken(clientSecret);
        Map<String, Object> response = restClientBuilder.build().post()
                .uri(APPLE_USER_MIGRATION_URI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("Authorization", "Bearer " + accessToken)
                .body(userExchangeRequestBody(transferSub, clientSecret))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        (request, res) -> {
                            String body = new String(res.getBody().readAllBytes());
                            log.error("Apple usermigrationinfo(교환) 호출 실패 - status: {}, errorCode: {}",
                                    res.getStatusCode(), extractErrorCode(body));
                            throw HttpClientErrorException.create(
                                    res.getStatusCode(), res.getStatusText(), res.getHeaders(), body.getBytes(), null);
                        })
                .body(new ParameterizedTypeReference<>() {
                });
        return requiredString(response, "sub");
    }

    private String requestExchangeAccessToken(String clientSecret) {
        Map<String, Object> response = restClientBuilder.build().post()
                .uri(APPLE_TOKEN_URI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(accessTokenRequestBody(clientSecret))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
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

    private MultiValueMap<String, String> userExchangeRequestBody(String transferSub, String clientSecret) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("transfer_sub", transferSub);
        body.add("client_id", properties.clientId());
        body.add("client_secret", clientSecret);
        return body;
    }

    private AppleClientSecretSupplier appleClientSecretSupplier() {
        return new AppleClientSecretSupplier(
                properties.teamId(),
                properties.keyId(),
                properties.clientId(),
                new ApplePrivateKeyLoader().loadFromPem(properties.privateKey())
        );
    }

    private String requiredString(Map<String, Object> response, String key) {
        if (response == null || !(response.get(key) instanceof String value) || !StringUtils.hasText(value)) {
            throw new IllegalStateException("Apple 새 팀 sub 교환 응답에 " + key + " 값이 없습니다.");
        }
        return value;
    }

    private String extractErrorCode(String body) {
        if (body == null) {
            return null;
        }
        int start = body.indexOf("\"error\":\"");
        if (start < 0) {
            return null;
        }
        start += "\"error\":\"".length();
        int end = body.indexOf('"', start);
        if (end < 0) {
            return null;
        }
        return body.substring(start, end);
    }
}
