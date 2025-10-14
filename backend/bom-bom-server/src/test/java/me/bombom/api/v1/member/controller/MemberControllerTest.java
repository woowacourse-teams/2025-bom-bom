package me.bombom.api.v1.member.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.dto.request.MemberInfoUpdateRequest;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
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
    private Authentication authentication;

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
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(attributes, member, null, null);

        authentication = new OAuth2AuthenticationToken(
                customOAuth2User,
                customOAuth2User.getAuthorities(),
                "registrationId"
        );
    }

    @Test
    void 형식에_맞지_않는_닉네임으로_변경_시도_시_400_예외가_발생한다() throws Exception {
        // given
        MemberInfoUpdateRequest request = new MemberInfoUpdateRequest("invalid..nickname", null, null, null);
        String requestBody = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(patch("/api/v1/members/me")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }
    
    @Test
    void 너무_짧은_닉네임으로_변경_시도_시_400_예외가_발생한다() throws Exception {
        // given
        MemberInfoUpdateRequest request = new MemberInfoUpdateRequest("a", null, null, null);
        String requestBody = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(patch("/api/v1/members/me")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void 형식에_맞는_닉네임이면_정상_동작한다() throws Exception {
        // given
        MemberInfoUpdateRequest request = new MemberInfoUpdateRequest("new.nickname", null, null, null);
        String requestBody = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(patch("/api/v1/members/me")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andDo(print());
    }
}
