package me.bombom.api.v1.member.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.dto.request.MemberProfileUpdateRequest;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    private Member member;
    private CustomOAuth2User customOAuth2User;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAllInBatch();
        member = TestFixture.normalMemberFixture();
        memberRepository.save(member);

        Map<String, Object> attributes = Map.of(
                "id", member.getId().toString(),
                "email", member.getEmail(),
                "name", member.getNickname()
        );
        customOAuth2User = new CustomOAuth2User(attributes, member, null, null);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthentication() {
        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                customOAuth2User,
                customOAuth2User.getAuthorities(),
                "registrationId"
        );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authToken);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void 형식에_맞지_않는_닉네임으로_변경_시도_시_400_예외가_발생한다() throws Exception {
        // given
        setAuthentication();
        MemberProfileUpdateRequest request = new MemberProfileUpdateRequest("invalid..nickname", null, null, null);
        String requestBody = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(patch("/api/v1/members/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }
    
    @Test
    void 너무_짧은_닉네임으로_변경_시도_시_400_예외가_발생한다() throws Exception {
        // given
        setAuthentication();
        MemberProfileUpdateRequest request = new MemberProfileUpdateRequest("a", null, null, null);
        String requestBody = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(patch("/api/v1/members/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void 형식에_맞는_닉네임이면_정상_동작한다() throws Exception {
        // given
        setAuthentication();
        MemberProfileUpdateRequest request = new MemberProfileUpdateRequest("new.nickname", null, null, null);
        String requestBody = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(patch("/api/v1/members/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andDo(print());
    }
}
