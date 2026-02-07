package me.bombom.api.v1.challenge.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeComment;
import me.bombom.api.v1.challenge.domain.ChallengeCommentReply;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.dto.request.CreateCommentReplyRequest;
import me.bombom.api.v1.challenge.repository.ChallengeCommentReplyRepository;
import me.bombom.api.v1.challenge.repository.ChallengeCommentRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.NewsletterGroup;
import me.bombom.api.v1.newsletter.repository.NewsletterGroupRepository;
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

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ChallengeParticipantRepository challengeParticipantRepository;

    @Autowired
    private ChallengeCommentRepository challengeCommentRepository;

    @Autowired
    private ChallengeCommentReplyRepository challengeCommentReplyRepository;

    @Autowired
    private NewsletterGroupRepository newsletterGroupRepository;

    private OAuth2AuthenticationToken authToken;
    private Member viewer;
    private Challenge challenge;
    private ChallengeComment challengeComment;

    @BeforeEach
    void setUp() {
        challengeCommentReplyRepository.deleteAllInBatch();
        challengeCommentRepository.deleteAllInBatch();
        challengeParticipantRepository.deleteAllInBatch();
        challengeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        newsletterGroupRepository.deleteAllInBatch();

        viewer = memberRepository.save(
                TestFixture.createUniqueMember("replyUser", java.util.UUID.randomUUID().toString()));
        Member commentAuthor = memberRepository.save(
                TestFixture.createUniqueMember("commentAuthor", java.util.UUID.randomUUID().toString()));

        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);
        challenge = challengeRepository.save(
                TestFixture.createChallenge(
                        "comment-challenge",
                        java.time.LocalDate.now().minusDays(1),
                        java.time.LocalDate.now().plusDays(5),
                        7,
                        group.getId()));

        ChallengeParticipant commentAuthorParticipant = challengeParticipantRepository.save(
                TestFixture.createChallengeParticipant(
                        challenge.getId(),
                        commentAuthor.getId(),
                        0));

        ChallengeParticipant viewerParticipant = challengeParticipantRepository.save(
                TestFixture.createChallengeParticipant(
                        challenge.getId(),
                        viewer.getId(),
                        0));

        challengeComment = challengeCommentRepository.save(
                TestFixture.createChallengeComment(
                        1L,
                        commentAuthorParticipant.getId(),
                        "article title",
                        "quote",
                        "comment"));

        challengeCommentReplyRepository.save(
                ChallengeCommentReply.builder()
                        .commentId(challengeComment.getId())
                        .participantId(viewerParticipant.getId())
                        .reply("첫번째 답글")
                        .build());

        Map<String, Object> attributes = Map.of(
                "id", viewer.getId().toString(),
                "email", viewer.getEmail(),
                "name", viewer.getNickname()
        );

        CustomOAuth2User principal = new CustomOAuth2User(attributes, viewer, null, null);
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
        mockMvc.perform(post("/api/v1/challenges/{challengeId}/comments/{commentId}/replies", challenge.getId(), 0L)
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
        mockMvc.perform(post("/api/v1/challenges/{challengeId}/comments/{commentId}/replies", challenge.getId(), challengeComment.getId())
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
        mockMvc.perform(post("/api/v1/challenges/{challengeId}/comments/{commentId}/replies", challenge.getId(), challengeComment.getId())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 코멘트_답글_목록을_조회하면_200과_페이지정보를_반환한다() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/challenges/{challengeId}/comments/{commentId}/replies", challenge.getId(), challengeComment.getId())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken))
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].reply", is("첫번째 답글")))
                .andExpect(jsonPath("$.content[0].isMyReply", is(true)));
    }

    @Test
    void 코멘트ID가_1미만이면_답글_조회시_400을_응답한다() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/challenges/{challengeId}/comments/{commentId}/replies", challenge.getId(), 0L)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
