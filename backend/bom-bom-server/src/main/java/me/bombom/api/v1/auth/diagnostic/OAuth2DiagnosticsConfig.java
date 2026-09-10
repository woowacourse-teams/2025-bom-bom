package me.bombom.api.v1.auth.diagnostic;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OAuth2DiagnosticsConfig {
    @Bean
    public DiagnosticOAuth2TokenResponseClient diagnosticGoogleTokenClient(OAuth2Diagnostics diagnostics) {
        return new DiagnosticOAuth2TokenResponseClient(RestClient.builder(), diagnostics);
    }

    @Bean
    public DiagnosticOAuth2UserService diagnosticGoogleUserService(OAuth2Diagnostics diagnostics) {
        return new DiagnosticOAuth2UserService(new RestTemplate(), diagnostics);
    }
}
