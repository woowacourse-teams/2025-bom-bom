package me.bombom.api.v1.common.config;

import java.security.interfaces.ECPrivateKey;
import java.util.List;
import java.util.function.Supplier;
import me.bombom.api.v1.auth.AppleClientSecretSupplier;
import me.bombom.api.v1.auth.AppleOAuth2AccessTokenResponseClient;
import me.bombom.api.v1.auth.ApplePrivateKeyLoader;
import me.bombom.api.v1.auth.handler.OAuth2LoginSuccessHandler;
import me.bombom.api.v1.auth.service.AppleOAuth2Service;
import me.bombom.api.v1.auth.service.CustomOAuth2UserService;
import me.bombom.api.v1.auth.service.GoogleOAuth2LoginService;
import me.bombom.api.v1.auth.service.OAuth2LoginService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestClient;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${swagger.admin.username}")
    private String adminUsername;

    @Value("${swagger.admin.password}")
    private String adminPassword;

    @Bean
    public SecurityFilterChain apiSecurityFilterChain(
            HttpSecurity http,
            CustomOAuth2UserService customOAuth2UserService,
            OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler,
            AppleOAuth2AccessTokenResponseClient appleOAuth2AccessTokenResponseClient,
            Supplier<String> appleClientSecretSupplier,   // 추가
            RestClient restClient                        // 추가
    ) throws Exception {
        // Spring 기본 클라이언트 (Apple 이외 공급자용)
        var defaultTokenClient =
                new org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient();

        // Apple 전용 위임 토큰 클라이언트
        OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> tokenClient =
            authorizationGrantRequest -> {
                String registrationId = authorizationGrantRequest.getClientRegistration().getRegistrationId();
                if (!"apple".equals(registrationId)) {
                    return defaultTokenClient.getTokenResponse(authorizationGrantRequest);
                }

                var clientRegistration = authorizationGrantRequest.getClientRegistration();
                var authzExchange = authorizationGrantRequest.getAuthorizationExchange();

                // Apple 요구 포맷 (x-www-form-urlencoded)
                org.springframework.util.LinkedMultiValueMap<String, String> params =
                        new org.springframework.util.LinkedMultiValueMap<>();
                params.add("client_id", clientRegistration.getClientId());
                params.add("client_secret", appleClientSecretSupplier.get()); // 호출 "직전" 동적 생성
                params.add("grant_type", "authorization_code");
                params.add("code", authzExchange.getAuthorizationResponse().getCode());
                params.add("redirect_uri", authzExchange.getAuthorizationRequest().getRedirectUri());

                String tokenUri = clientRegistration.getProviderDetails().getTokenUri();

                // Apple 응답(JSON Map) 수신
                java.util.Map<String, Object> body = restClient.post()
                        .uri(tokenUri)
                        .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                        .body(params)
                        .retrieve()
                        .body(new org.springframework.core.ParameterizedTypeReference<java.util.Map<String, Object>>() {
                        });

                if (body == null || !body.containsKey("access_token")) {
                    throw new org.springframework.security.oauth2.core.OAuth2AuthenticationException(
                            new org.springframework.security.oauth2.core.OAuth2Error("invalid_token_response"),
                            "Apple token response is null or missing access_token");
                }

                String accessToken = (String) body.get("access_token");
                String tokenTypeValue = (String) body.getOrDefault("token_type", "Bearer");
                org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType tokenType =
                        "bearer".equalsIgnoreCase(tokenTypeValue)
                                ? org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER
                                : org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER;

                long expiresIn = 0L;
                Object expiresObj = body.get("expires_in");
                if (expiresObj instanceof Number n) {
                    expiresIn = n.longValue();
                } else if (expiresObj instanceof String s) {
                    try {
                        expiresIn = Long.parseLong(s);
                    } catch (NumberFormatException ignore) {
                    }
                }

                String refreshToken = (String) body.get("refresh_token");

                return org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse
                        .withToken(accessToken)
                        .tokenType(tokenType)
                        .expiresIn(expiresIn)
                        .scopes(clientRegistration.getScopes())
                        .refreshToken(refreshToken)
                        .additionalParameters(body)
                        .build();
            };

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2Login(oauth2 -> oauth2
                        .tokenEndpoint(token -> token.accessTokenResponseClient(appleOAuth2AccessTokenResponseClient))
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*"); // 모든 origin 허용, 필요에 따라 특정 origin으로 변경 가능
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*"); // 모든 헤더 허용
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
                .username(adminUsername)
                .password(passwordEncoder().encode(adminPassword))
                .roles("DEVELOPER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ECPrivateKey applePrivateKey(
            @Value("${oauth2.apple.private-key}") String privateKeyPem
    ) {
        return new ApplePrivateKeyLoader().loadFromPem(privateKeyPem);
    }

    @Bean
    public Supplier<String> appleClientSecretSupplier(
            @Value("${oauth2.apple.team-id}") String teamId,
            @Value("${oauth2.apple.key-id}") String keyId,
            @Value("${oauth2.apple.client-id}") String clientId,
            ECPrivateKey applePrivateKey
    ) {
        return new AppleClientSecretSupplier(teamId, keyId, clientId, applePrivateKey);
    }

    @Bean
    public AppleOAuth2AccessTokenResponseClient appleOAuth2AccessTokenResponseClient(
            Supplier<String> appleClientSecretSupplier,
            RestClient.Builder restClientBuilder
    ) {
        return new AppleOAuth2AccessTokenResponseClient(appleClientSecretSupplier, restClientBuilder.build());
    }

    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }

    @Bean
    public List<OAuth2LoginService> loginServices(
            GoogleOAuth2LoginService googleService,
            AppleOAuth2Service appleService
    ) {
        return List.of(googleService, appleService);
    }
}
