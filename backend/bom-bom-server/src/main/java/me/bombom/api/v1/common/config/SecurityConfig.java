package me.bombom.api.v1.common.config;

import java.security.interfaces.ECPrivateKey;
import java.time.Duration;
import java.util.List;
import me.bombom.api.v1.auth.AppleClientSecretGenerator;
import me.bombom.api.v1.auth.ApplePrivateKeyLoader;
import me.bombom.api.v1.auth.AppleTokenClient;
import me.bombom.api.v1.auth.client.AppleAuthClient;
import me.bombom.api.v1.auth.handler.OAuth2LoginFailureHandler;
import me.bombom.api.v1.auth.handler.OAuth2LoginSuccessHandler;
import me.bombom.api.v1.auth.resolver.AppleAuthorizationRequestResolver;
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
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
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

    @Value("${server.servlet.session.cookie.max-age}")
    private Duration cookieMaxAge;

    @Bean
    public SecurityFilterChain apiSecurityFilterChain(
            HttpSecurity http,
            CustomOAuth2UserService customOAuth2UserService,
            AppleOAuth2Service appleOAuth2Service,
            OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler,
            OAuth2LoginFailureHandler oAuth2LoginFailureHandler,
            OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> delegatingAccessTokenClient,
            ClientRegistrationRepository clientRegistrationRepository
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization ->
                                authorization.authorizationRequestResolver(new AppleAuthorizationRequestResolver(clientRegistrationRepository))
                        )
                        .tokenEndpoint(token -> token.accessTokenResponseClient(delegatingAccessTokenClient))
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                                .oidcUserService(appleOAuth2Service))
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler(oAuth2LoginFailureHandler));

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
    public AppleTokenClient appleTokenClient(
            AppleClientSecretGenerator appleClientSecretGenerator,
            RestClient.Builder restClientBuilder
    ) {
        return new AppleTokenClient(appleClientSecretGenerator, restClientBuilder.build());
    }

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> delegatingAccessTokenClient(
            AppleAuthClient appleClient
    ) {
        var defaultClient = new RestClientAuthorizationCodeTokenResponseClient();
        return request -> {
            String registrationId = request.getClientRegistration().getRegistrationId();
            if ("apple".equals(registrationId)) {
                return appleClient.getTokenResponse(request);
            }
            return defaultClient.getTokenResponse(request);
        };
    }

    @Bean
    public List<OAuth2LoginService> loginServices(
            GoogleOAuth2LoginService googleService
    ) {
        return List.of(googleService);
    }

    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.client.provider.apple.jwk-set-uri}") String jwkSetUri,
            @Value("${spring.security.oauth2.client.registration.apple.client-id}") String audience) {

        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromOidcIssuerLocation("https://appleid.apple.com");

        // Audience(aud) 클레임 검증을 위한 Validator 추가
        OAuth2TokenValidator<Jwt> audienceValidator = token -> {
            return token.getAudience().contains(audience)
                    ? org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.success()
                    : org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.failure(new org.springframework.security.oauth2.core.OAuth2Error("invalid_token", "The required audience is missing", null));
        };

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer("https://appleid.apple.com");
        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        jwtDecoder.setJwtValidator(withAudience);
        return jwtDecoder;
    }

    @Bean
    public JwtDecoderFactory<ClientRegistration> idTokenDecoderFactory(JwtDecoder jwtDecoder) {
        // JwtDecoderFactory를 Bean으로 직접 등록합니다.
        // Spring Security는 이 Bean을 자동으로 사용하여 ID 토큰을 검증합니다.
        return clientRegistration -> {
            if (clientRegistration.getRegistrationId().equals("apple")) {
                // "apple" 로그인일 경우, 우리가 만든 audience 검증 기능이 포함된 JwtDecoder를 사용합니다.
                return jwtDecoder;
            }
            // 다른 OIDC 제공자(예: Google)는 Spring Security의 기본 디코더를 사용합니다.
            return JwtDecoders.fromOidcIssuerLocation(clientRegistration.getProviderDetails().getIssuerUri());
        };
    }

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName("JSESSIONID");
        serializer.setUseHttpOnlyCookie(true);
        serializer.setUseSecureCookie(true);
        serializer.setSameSite("None");
        serializer.setCookiePath("/");
        serializer.setCookieMaxAge((int) cookieMaxAge.getSeconds());
        return serializer;
    }
}
