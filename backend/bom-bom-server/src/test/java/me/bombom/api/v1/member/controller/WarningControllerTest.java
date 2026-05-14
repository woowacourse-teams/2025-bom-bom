package me.bombom.api.v1.member.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.auth.handler.OAuth2LoginSuccessHandler;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.domain.WarningSetting;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.member.repository.WarningSettingRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
class WarningControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private WarningSettingRepository warningSettingRepository;

    private Member member;

    @MockitoBean
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    private OAuth2AuthenticationToken authToken;

    @BeforeEach
    void setUp() {
        warningSettingRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        member = TestFixture.normalMemberFixture();
        memberRepository.save(member);

        WarningSetting warningSetting = WarningSetting.builder()
                .memberId(member.getId())
                .isVisible(true)
                .build();
        warningSettingRepository.save(warningSetting);

        Map<String, Object> attributes = Map.of(
                "id", member.getId().toString(),
                "email", member.getEmail(),
                "name", member.getNickname()
        );
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(attributes, member, null, null);

        authToken = new OAuth2AuthenticationToken(
                customOAuth2User,
                customOAuth2User.getAuthorities(),
                "registrationId"
        );
    }

    @Test
    @DisplayName("임박 경고 설정 조회 성공")
    void nearCapacityWarningStatus_success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/members/me/warning/near-capacity")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isVisible").value(true));
    }

    @Test
    @DisplayName("임박 경고 설정 수정 성공")
    void updateWarningSetting_success() throws Exception {
        // when
        mockMvc.perform(post("/api/v1/members/me/warning/near-capacity")
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "isVisible": false
                                }
                                """))
                .andExpect(status().isNoContent());

        // then
        WarningSetting updatedSetting = warningSettingRepository.findByMemberId(member.getId()).orElseThrow();
        assertThat(updatedSetting.isVisible()).isFalse();
    }
}
