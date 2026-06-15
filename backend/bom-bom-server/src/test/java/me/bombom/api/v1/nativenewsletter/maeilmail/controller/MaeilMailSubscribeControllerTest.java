package me.bombom.api.v1.nativenewsletter.maeilmail.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.common.resolver.LoginMemberArgumentResolver;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.enums.Gender;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailUpdateSubscriptionRequest;
import me.bombom.api.v1.nativenewsletter.maeilmail.service.MaeilMailSubscribeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@WebMvcTest(controllers = MaeilMailSubscribeController.class)
@Import({MaeilMailSubscribeController.class, MaeilMailSubscribeControllerTest.TestConfig.class})
class MaeilMailSubscribeControllerTest {

    @Configuration
    @EnableWebSecurity
    static class TestConfig implements WebMvcConfigurer {

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

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(loginMemberArgumentResolver());
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MaeilMailSubscribeService maeilMailSubscribeService;

    private Member member;
    private OAuth2AuthenticationToken authToken;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .provider("google")
                .providerId("providerId")
                .email("email@bombom.news")
                .nickname("nickname")
                .gender(Gender.FEMALE)
                .roleId(1L)
                .build();
        authToken = createAuthToken(member);
    }

    @Test
    void 구독_생성과_수정은_put으로_요청한다() throws Exception {
        MaeilMailUpdateSubscriptionRequest request = new MaeilMailUpdateSubscriptionRequest(
                List.of(MaeilMailTrack.BE, MaeilMailTrack.FE)
        );

        mockMvc.perform(put("/api/v1/subscriptions/native/maeil-mail")
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk());

        verify(maeilMailSubscribeService).putSubscription(member, request);
    }

    @Test
    void 빈_트랙으로_put을_요청하면_400을_반환한다() throws Exception {
        mockMvc.perform(put("/api/v1/subscriptions/native/maeil-mail")
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new MaeilMailUpdateSubscriptionRequest(List.of()))))
                .andExpect(status().isBadRequest());

        verify(maeilMailSubscribeService, never()).putSubscription(any(), any());
    }

    @Test
    void 구독_삭제는_delete로_요청한다() throws Exception {
        mockMvc.perform(delete("/api/v1/subscriptions/native/maeil-mail")
                        .with(authentication(authToken)))
                .andExpect(status().isOk());

        verify(maeilMailSubscribeService).deleteSubscription(member.getId());
    }

    private OAuth2AuthenticationToken createAuthToken(Member member) {
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

    private String toJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
