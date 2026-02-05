package me.bombom.api.v1.subscribe.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.common.resolver.LoginMemberArgumentResolver;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.enums.Gender;
import me.bombom.api.v1.subscribe.dto.UnsubscribeResponse;
import me.bombom.api.v1.subscribe.service.SubscribeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfigureMockMvc
@WebMvcTest(controllers = SubscribeController.class)
@Import({SubscribeController.class, SubscribeControllerTest.TestConfig.class})
class SubscribeControllerTest {

    @Configuration
    @EnableWebSecurity
    static class TestConfig implements WebMvcConfigurer {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .exceptionHandling(ex -> ex
                            .authenticationEntryPoint((request, response, authException) ->
                                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                            )
                    )
                    .build();
        }

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new LoginMemberArgumentResolver());
        }
    }

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    SubscribeService subscribeService;

    private OAuth2AuthenticationToken authToken;

    @BeforeEach
    void setUp() {
        Member member = Member.builder()
                .id(1L)
                .provider("apple")
                .providerId("providerId")
                .email("email@bombom.news")
                .nickname("nickname")
                .gender(Gender.FEMALE)
                .roleId(1L)
                .build();

        Map<String, Object> attrs = Map.of(
                "id", "1",
                "email", "email@bombom.news",
                "name", "nickname"
        );

        CustomOAuth2User user = new CustomOAuth2User(attrs, member, null, null);
        authToken = new OAuth2AuthenticationToken(user, user.getAuthorities(), "registrationId");
    }

    @Test
    void 인증된_사용자는_구독_목록을_조회할_수_있다() throws Exception {
        given(subscribeService.getSubscribedNewsletters(any(Member.class)))
                .willReturn(List.of());

        mockMvc.perform(get("/api/v1/members/me/subscriptions")
                        .with(authentication(authToken)))
                .andExpect(status().isOk());
    }

    @Test
    void 인증되지_않은_사용자는_403을_반환한다() throws Exception {
        mockMvc.perform(get("/api/v1/members/me/subscriptions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 인증된_사용자는_구독_취소를_요청할_수_있다() throws Exception {
        given(subscribeService.unsubscribe(anyLong(), anyLong()))
                .willReturn(UnsubscribeResponse.of(null));

        mockMvc.perform(post("/api/v1/members/me/subscriptions/{id}/unsubscribe", 1L)
                        .with(authentication(authToken)))
                .andExpect(status().isOk());
    }
}
