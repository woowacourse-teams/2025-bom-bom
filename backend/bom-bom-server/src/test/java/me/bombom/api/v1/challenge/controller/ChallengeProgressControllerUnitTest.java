package me.bombom.api.v1.challenge.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.challenge.domain.ChallengeTodoStatus;
import me.bombom.api.v1.challenge.domain.ChallengeTodoType;
import me.bombom.api.v1.challenge.dto.MemberChallengeProgressResponse;
import me.bombom.api.v1.challenge.dto.TodayTodoResponse;
import me.bombom.api.v1.challenge.service.ChallengeProgressService;
import me.bombom.api.v1.common.config.WebConfig;
import me.bombom.api.v1.member.domain.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@Import(WebConfig.class)
@WebMvcTest(ChallengeProgressController.class)
class ChallengeProgressControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChallengeProgressService challengeProgressService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

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

        Map<String, Object> attributes = Map.of(
                "id", member.getId().toString(),
                "email", member.getEmail(),
                "name", member.getNickname());
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(attributes, member, null, null);

        authentication = new OAuth2AuthenticationToken(
                customOAuth2User,
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                "google"
        );
    }

    @Test
    void 챌린지_사용자_진행도_조회_성공() throws Exception {
        // given
        Long challengeId = 1L;
        MemberChallengeProgressResponse response = new MemberChallengeProgressResponse(
                "tester",
                20,
                5,
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
}
