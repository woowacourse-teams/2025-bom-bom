package me.bombom.api.v1.challenge.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.challenge.dto.response.ChallengeInfoResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeLandingNewsletterResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeLandingResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeTeamListResponse;
import me.bombom.api.v1.challenge.service.ChallengeService;
import me.bombom.api.v1.common.exception.GlobalExceptionHandler;
import me.bombom.api.v1.member.domain.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@WithMockUser
@WebMvcTest(ChallengeController.class)
@Import({ChallengeController.class, GlobalExceptionHandler.class, ChallengeControllerUnitTest.TestConfig.class})
class ChallengeControllerUnitTest {

    @Configuration
    @EnableWebSecurity
    static class TestConfig implements WebMvcConfigurer {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(MethodParameter parameter) {
                    return parameter.getParameterType().equals(Member.class);
                }

                @Override
                public Object resolveArgument(
                        MethodParameter parameter,
                        ModelAndViewContainer mavContainer,
                        NativeWebRequest webRequest,
                        WebDataBinderFactory binderFactory
                ) {
                    return null;
                }
            });
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChallengeService challengeService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void 챌린지_상세_정보를_요청할_수_있다() throws Exception {
        //given
        ChallengeInfoResponse response = new ChallengeInfoResponse(
                "챌린지1",
                LocalDate.of(2026, 1, 5),
                LocalDate.of(2026, 2, 4),
                1,
                24,
                19
        );

        given(challengeService.getChallengeInfo(1L))
                .willReturn(response);

        //when & then
        mockMvc.perform(get("/api/v1/challenges/{id}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("챌린지1"));
    }

    @Test
    void 챌린지_랜딩_정보를_요청할_수_있다() throws Exception {
        //given
        ChallengeLandingResponse response = new ChallengeLandingResponse(
                "챌린지1",
                1,
                LocalDate.of(2026, 1, 5),
                LocalDate.of(2026, 2, 4),
                true,
                List.of(
                        new ChallengeLandingNewsletterResponse(
                                1L,
                                "뉴스레터1",
                                "https://image.url",
                                "카테고리",
                                "설명"
                        )
                )
        );

        given(challengeService.getChallengeLanding(1L))
                .willReturn(response);

        //when & then
        mockMvc.perform(get("/api/v1/challenges/{id}/landing", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("챌린지1"))
                .andExpect(jsonPath("$.generation").value(1))
                .andExpect(jsonPath("$.newsletters").isArray())
                .andExpect(jsonPath("$.grantsBadge").value(true));
    }

    @Test
    void 챌린지_상세_정보를_요청시_음수_id면_400() throws Exception {
        //when & then
        mockMvc.perform(get("/api/v1/challenges/{id}", -1L))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void 챌린지_신청_가능_여부를_요청시_음수_id면_400() throws Exception {
        //when & then
        mockMvc.perform(get("/api/v1/challenges/{id}/eligibility", -1L))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void 챌린지_신청시_음수_id면_400() throws Exception {
        //when & then
        mockMvc.perform(post("/api/v1/challenges/{id}/application", -1L))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void 챌린지_취소시_음수_id면_400() throws Exception {
        //when & then
        mockMvc.perform(delete("/api/v1/challenges/{id}/application", -1L))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void 팀_목록_조회_시_내_팀_포함() throws Exception {
        // given
        List<ChallengeTeamListResponse.TeamInfoResponse> teams = List.of(
                new ChallengeTeamListResponse.TeamInfoResponse(1L, 1, false),
                new ChallengeTeamListResponse.TeamInfoResponse(2L, 2, true),
                new ChallengeTeamListResponse.TeamInfoResponse(3L, 3, false)
        );
        ChallengeTeamListResponse response = new ChallengeTeamListResponse(3, 2L, teams);

        given(challengeService.getTeamList(1L, null))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/challenges/{id}/teams", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTeamCount").value(3))
                .andExpect(jsonPath("$.myTeamId").value(2))
                .andExpect(jsonPath("$.teams").isArray())
                .andExpect(jsonPath("$.teams[0].teamId").value(1))
                .andExpect(jsonPath("$.teams[0].teamNumber").value(1))
                .andExpect(jsonPath("$.teams[0].isMyTeam").value(false))
                .andExpect(jsonPath("$.teams[1].teamId").value(2))
                .andExpect(jsonPath("$.teams[1].teamNumber").value(2))
                .andExpect(jsonPath("$.teams[1].isMyTeam").value(true))
                .andExpect(jsonPath("$.teams[2].teamId").value(3))
                .andExpect(jsonPath("$.teams[2].teamNumber").value(3))
                .andExpect(jsonPath("$.teams[2].isMyTeam").value(false));
    }

    @Test
    void 팀_목록_조회시_음수_id면_400() throws Exception {
        //when & then
        mockMvc.perform(get("/api/v1/challenges/{id}/teams", -1L))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
