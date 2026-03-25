package me.bombom.api.v1.newsletter.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import me.bombom.api.v1.common.resolver.LoginMemberArgumentResolver;
import me.bombom.api.v1.newsletter.domain.NewsletterPublicationStatus;
import me.bombom.api.v1.newsletter.dto.CategoryResponse;
import me.bombom.api.v1.newsletter.dto.NewsletterResponse;
import me.bombom.api.v1.newsletter.dto.NewslettersResponse;
import me.bombom.api.v1.newsletter.service.NewsletterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@WebMvcTest(controllers = NewsletterController.class)
@Import({NewsletterController.class, NewsletterControllerTest.TestConfig.class})
class NewsletterControllerTest {

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
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(loginMemberArgumentResolver());
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NewsletterService newsletterService;

    @Test
    void 유효하지_않은_토큰이어도_뉴스레터_목록을_정상_반환한다() throws Exception {
        NewslettersResponse response = NewslettersResponse.of(
                List.of(new CategoryResponse(10L, "테크")),
                List.of(new NewsletterResponse(
                        1L,
                        "봄봄 뉴스",
                        "https://image.url",
                        "설명",
                        "https://subscribe.url",
                        10L,
                        "테크",
                        NewsletterPublicationStatus.ACTIVE,
                        false
                ))
        );
        TestingAuthenticationToken invalidAuth = new TestingAuthenticationToken("notCustomUser", null);
        invalidAuth.setAuthenticated(true);

        given(newsletterService.getNewsletters(eq(null), eq(false), eq(null)))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/newsletters").with(authentication(invalidAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories[0].id").value(10L))
                .andExpect(jsonPath("$.categories[0].name").value("테크"))
                .andExpect(jsonPath("$.newsletters[0].newsletterId").value(1L))
                .andExpect(jsonPath("$.newsletters[0].isSubscribed").value(false));
    }
}
