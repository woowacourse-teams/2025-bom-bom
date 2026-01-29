package me.bombom.api.v1.challenge.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.challenge.dto.request.CreateCommentReplyRequest;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
class ChallengeCommentReplyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    private OAuth2AuthenticationToken authToken;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAllInBatch();

        Member member = memberRepository.save(
                TestFixture.createUniqueMember("replyUser", java.util.UUID.randomUUID().toString()));

        Map<String, Object> attributes = Map.of(
                "id", member.getId().toString(),
                "email", member.getEmail(),
                "name", member.getNickname()
        );

        CustomOAuth2User principal = new CustomOAuth2User(attributes, member, null, null);
        authToken = new OAuth2AuthenticationToken(
                principal,
                principal.getAuthorities(),
                "registrationId"
        );
    }

    @Test
    void commentId가_1_미만이면_400을_응답한다() throws Exception {
        // given
        CreateCommentReplyRequest request = new CreateCommentReplyRequest("감사합니다!");

        // when & then
        mockMvc.perform(post("/api/v1/challenges/comments/{commentId}/replies", 0L)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 답글_내용이_null이면_400을_응답한다() throws Exception {
        // given
        CreateCommentReplyRequest request = new CreateCommentReplyRequest(null);

        // when & then
        mockMvc.perform(post("/api/v1/challenges/comments/{commentId}/replies", 1L)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 답글_내용이_500자를_초과하면_400을_응답한다() throws Exception {
        // given
        String longReply = "a".repeat(501);
        CreateCommentReplyRequest request = new CreateCommentReplyRequest(longReply);

        // when & then
        mockMvc.perform(post("/api/v1/challenges/comments/{commentId}/replies", 1L)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
