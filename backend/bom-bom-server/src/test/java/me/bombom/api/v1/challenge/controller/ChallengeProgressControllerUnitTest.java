package me.bombom.api.v1.challenge.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeDailyStatus;
import me.bombom.api.v1.challenge.domain.ChallengeGrade;
import me.bombom.api.v1.challenge.domain.ChallengeTodoStatus;
import me.bombom.api.v1.challenge.domain.ChallengeTodoType;
import me.bombom.api.v1.challenge.dto.TeamChallengeProgressFlat;
import me.bombom.api.v1.challenge.dto.response.CertificationInfoResponse;
import me.bombom.api.v1.challenge.dto.response.MemberChallengeProgressResponse;
import me.bombom.api.v1.challenge.dto.response.TeamChallengeProgressResponse;
import me.bombom.api.v1.challenge.dto.response.TodayTodoResponse;
import me.bombom.api.v1.challenge.service.ChallengeProgressService;
import me.bombom.api.v1.common.exception.GlobalExceptionHandler;
import me.bombom.api.v1.common.resolver.LoginMemberArgumentResolver;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@Import({
        ChallengeProgressController.class,
        GlobalExceptionHandler.class,
        ChallengeProgressControllerUnitTest.TestConfig.class
})
@WebMvcTest(value = ChallengeProgressController.class, properties = "LOG_PATH=build/logs")
class ChallengeProgressControllerUnitTest {

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
    private ChallengeProgressService challengeProgressService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private MemberRepository memberRepository;

    private Authentication authentication;

    @BeforeEach
    void setUp() {
        Member member = Member.builder()
                .id(1L)
                .nickname("tester")
                .email("test@example.com")
                .provider("google")
                .providerId("12345")
                .gender(me.bombom.api.v1.member.enums.Gender.MALE)
                .roleId(1L)
                .build();

        given(memberRepository.findById(member.getId())).willReturn(java.util.Optional.of(member));

        Map<String, Object> attributes = Map.of(
                "id", member.getId().toString(),
                "email", member.getEmail(),
                "name", member.getNickname());
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(attributes, member, null, null);
        authentication = new OAuth2AuthenticationToken(
                customOAuth2User,
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                "google");
    }

    @Test
    void 챌린지_사용자_진행도_조회_성공() throws Exception {
        // given
        Long challengeId = 1L;
        MemberChallengeProgressResponse response = new MemberChallengeProgressResponse(
                "tester",
                20,
                true,
                5,
                3,
                1,
                List.of(new TodayTodoResponse(ChallengeTodoType.READ, ChallengeTodoStatus.COMPLETE)));

        given(challengeProgressService.getMemberProgress(eq(challengeId), any(Member.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/challenges/{id}/progress/me", challengeId)
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("tester"))
                .andExpect(jsonPath("$.totalDays").value(20))
                .andExpect(jsonPath("$.completedDays").value(5))
                .andExpect(jsonPath("$.todayTodos[0].challengeTodoType").value("READ"))
                .andExpect(jsonPath("$.todayTodos[0].challengeTodoStatus").value("COMPLETE"))
                .andDo(print());
    }

    @Test
    void 챌린지_사용자_진행도_조회_실패_ID가_양수가_아님() throws Exception {
        // given
        Long invalidId = -1L;

        // when & then
        mockMvc.perform(get("/api/v1/challenges/{id}/progress/me", invalidId)
                        .with(authentication(authentication)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void 특정_팀_진행도_조회_성공() throws Exception {
        // given
        Long challengeId = 1L;
        Long teamId = 10L;

        Challenge challenge = Challenge.builder()
                .id(challengeId)
                .name("Test Challenge")
                .generation(1)
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().plusDays(5))
                .totalDays(10)
                .newsletterGroupId(1L)
                .build();

        TeamChallengeProgressFlat progressFlat = new TeamChallengeProgressFlat(
                1L, "tester", true, 5, 10, 77, LocalDate.now(), ChallengeDailyStatus.COMPLETE);

        TeamChallengeProgressResponse response = TeamChallengeProgressResponse.of(
                challenge,
                List.of(progressFlat)
        );

        given(challengeProgressService.getTeamProgressByTeamId(eq(challengeId), eq(teamId), any(Member.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/challenges/{id}/progress/teams/{teamId}", challengeId, teamId)
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.challenge").exists())
                .andExpect(jsonPath("$.teamSummary").exists())
                .andExpect(jsonPath("$.teamSummary.achievementAverage").value(77))
                .andExpect(jsonPath("$.members").isArray())
                .andDo(print());
    }

    @Test
    void 특정_팀_진행도_조회_실패_챌린지_ID가_양수가_아님() throws Exception {
        // given
        Long invalidId = -1L;
        Long teamId = 10L;

        // when & then
        mockMvc.perform(get("/api/v1/challenges/{id}/progress/teams/{teamId}", invalidId, teamId)
                        .with(authentication(authentication)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void 특정_팀_진행도_조회_실패_팀_ID가_양수가_아님() throws Exception {
        // given
        Long challengeId = 1L;
        Long invalidTeamId = -1L;

        // when & then
        mockMvc.perform(get("/api/v1/challenges/{id}/progress/teams/{teamId}", challengeId, invalidTeamId)
                        .with(authentication(authentication)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void 수료증_정보_조회_성공() throws Exception {
        // given
        Long challengeId = 1L;
        CertificationInfoResponse response = new CertificationInfoResponse(
                "tester",
                "Test Challenge",
                1,
                LocalDate.now().minusDays(10),
                LocalDate.now().minusDays(1),
                ChallengeGrade.GOLD,
                100
        );

        given(challengeProgressService.getCertificationInfo(eq(challengeId), eq(1L)))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/challenges/{id}/certification", challengeId)
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("tester"))
                .andExpect(jsonPath("$.challengeName").value("Test Challenge"))
                .andExpect(jsonPath("$.medal").value("GOLD"))
                .andDo(print());
    }
}
