package me.bombom.api.v1.subscribe.client;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class WebRiskClient {

    private final RestClient restClient;
    private final String apiKey;

    public WebRiskClient(RestClient.Builder builder,
            @Value("${google.web-risk.api-key:${GOOGLE_WEB_RISK_API_KEY:}}") String apiKey,
            @Value("${google.web-risk.connect-timeout:3s}") Duration connectTimeout,
            @Value("${google.web-risk.read-timeout:5s}") Duration readTimeout) {
        this.apiKey = apiKey;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        this.restClient = builder.clone()
                .baseUrl("https://webrisk.googleapis.com")
                .requestFactory(factory)
                .build();
    }

    public boolean isThreat(String url) {
        if (apiKey == null || apiKey.isBlank()) {
            return false;
        }
        try {
            JsonNode response = restClient.get()
                    .uri(builder -> builder.path("/v1/uris:search")
                            .queryParam("threatTypes", "SOCIAL_ENGINEERING", "MALWARE", "UNWANTED_SOFTWARE")
                            .queryParam("uri", "{target}")
                            .build(url))
                    .header("X-Goog-Api-Key", apiKey)
                    .retrieve()
                    .onStatus(status -> !status.is2xxSuccessful(), (request, httpResponse) -> {
                        throw new WebRiskUnavailableException();
                    })
                    .body(JsonNode.class);
            if (response == null || !response.isObject()) {
                throw new WebRiskUnavailableException();
            }
            if (response.isEmpty()) {
                return false;
            }
            // Only an explicit empty lookup result may proceed. Unexpected responses fail closed.
            JsonNode threatTypes = response.path("threat").path("threatTypes");
            if (!threatTypes.isArray() || threatTypes.isEmpty()) {
                throw new WebRiskUnavailableException();
            }
            return true;
        } catch (Exception e) {
            // Do not propagate request URLs, personal unsubscribe tokens, keys or response bodies.
            throw new WebRiskUnavailableException();
        }
    }
}
