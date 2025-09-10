package me.bombom.api.v1.auth.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class AppleRevokeClient implements RevokeClient {

    private static final String APPLE_REVOKE_URL = "https://appleid.apple.com/auth/revoke";
    private static final String TOKEN_TYPE_HINT = "refresh_token";

    private final RestClient restClient;

    @Value("${oauth2.apple.client-id}")
    private String clientId;


    @Override
    public RevokeResult revoke(String token, String clientSecret) {
        MultiValueMap<String, String> formData = createRevokeFormData(token, clientSecret);
        restClient.post()
                .uri(APPLE_REVOKE_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .toBodilessEntity();
        return RevokeResult.ofSuccess();
    }

    private MultiValueMap<String, String> createRevokeFormData(String refreshToken, String clientSecret) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("token", refreshToken);
        formData.add("token_type_hint", TOKEN_TYPE_HINT);
        return formData;
    }
}
