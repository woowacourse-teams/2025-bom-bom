package me.bombom.api.v1.common.config;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import me.bombom.api.v1.common.resolver.LoginMemberArgumentResolver;
import java.util.Map;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.member.domain.Member;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@TestConfiguration
@EnableWebSecurity
public class ControllerTestConfig implements WebMvcConfigurer {

    public static OAuth2AuthenticationToken authToken(Member member) {
        Map<String, Object> attributes = Map.of(
                "id", member.getId().toString(),
                "email", member.getEmail(),
                "name", member.getNickname()
        );

        CustomOAuth2User customOAuth2User = new CustomOAuth2User(attributes, member, null, null);

        return new OAuth2AuthenticationToken(
                customOAuth2User,
                customOAuth2User.getAuthorities(),
                "registrationId"
        );
    }

    @Bean
    LoginMemberArgumentResolver loginMemberArgumentResolver() {
        return new LoginMemberArgumentResolver("JSESSIONID", "");
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response,
                                                   authException) -> response.sendError(
                                HttpServletResponse.SC_UNAUTHORIZED)))
                .build();
    }

    @Bean(name = "jpaMappingContext")
    JpaMetamodelMappingContext jpaMappingContext() {
        return Mockito.mock(JpaMetamodelMappingContext.class);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginMemberArgumentResolver());
    }
}
