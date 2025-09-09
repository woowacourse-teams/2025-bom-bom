package me.bombom.api.v1.common.config;

import java.security.interfaces.ECPrivateKey;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.auth.AppleAuthRequestEntityConverter;
import me.bombom.api.v1.auth.AppleClientSecretSupplier;
import me.bombom.api.v1.auth.ApplePrivateKeyLoader;
import me.bombom.api.v1.auth.handler.OAuth2LoginSuccessHandler;
import me.bombom.api.v1.auth.service.CustomOAuth2UserService;
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
import org.springframework.security.oauth2.client.endpoint.DefaultOAuth2TokenRequestParametersConverter;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Value("${swagger.admin.username}")
    private String adminUsername;

    @Value("${swagger.admin.password}")
    private String adminPassword;

    @Bean
    public SecurityFilterChain apiSecurityFilterChain(
            HttpSecurity http,
            AppleAuthRequestEntityConverter appleConverter) throws Exception {
        var tokenClient = new RestClientAuthorizationCodeTokenResponseClient();
        var requestEntityConverter = new DefaultOAuth2TokenRequestParametersConverter();
        tokenClient.setParametersConverter(request -> {
            String registrationId = request.getClientRegistration().getRegistrationId();
            if ("apple".equals(registrationId)) {
                return appleConverter.convert(request);
            }
            return requestEntityConverter.convert(request);
        });

        http
                // 모든 설정 열어둠
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(FrameOptionsConfig::disable))
                .cors(configurer -> configurer.configurationSource(corsConfigurationSource()))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2Login(oauth2 -> oauth2
                        .tokenEndpoint(token -> token.accessTokenResponseClient(tokenClient))
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
    public AppleAuthRequestEntityConverter appleAuthRequestEntityConverter(Supplier<String> appleClientSecretSupplier) {
        return new AppleAuthRequestEntityConverter(appleClientSecretSupplier);
    }
}
