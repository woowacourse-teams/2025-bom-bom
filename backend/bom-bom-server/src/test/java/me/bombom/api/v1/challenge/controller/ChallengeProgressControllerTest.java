package me.bombom.api.v1.challenge.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeDailyResult;
import me.bombom.api.v1.challenge.domain.ChallengeDailyStatus;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeTeam;
import me.bombom.api.v1.challenge.repository.ChallengeDailyResultRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTeamRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
class ChallengeProgressControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private MemberRepository memberRepository;

        @Autowired
        private ChallengeRepository challengeRepository;

        @Autowired
        private ChallengeParticipantRepository challengeParticipantRepository;

        @Autowired
        private ChallengeTeamRepository challengeTeamRepository;

        @Autowired
        private ChallengeDailyResultRepository challengeDailyResultRepository;

        private Member memberA;
        private Challenge challenge;
        private OAuth2AuthenticationToken authToken;

        @AfterEach
        void tearDown() {
                challengeDailyResultRepository.deleteAllInBatch();
                challengeParticipantRepository.deleteAllInBatch();
                challengeTeamRepository.deleteAllInBatch();
                challengeRepository.deleteAllInBatch();
                memberRepository.deleteAllInBatch();
        }

        @BeforeEach
        void setUp() {
                memberA = memberRepository.save(TestFixture.createUniqueMember("userA", "A"));

                challenge = challengeRepository.save(TestFixture.createChallenge(
                                "Test Challenge",
                                LocalDate.now().minusDays(5),
                                LocalDate.now().plusDays(5),
                                10));

                Map<String, Object> attributes = Map.of(
                                "id", memberA.getId().toString(),
                                "email", memberA.getEmail(),
                                "name", memberA.getNickname());

                CustomOAuth2User customOAuth2User = new CustomOAuth2User(attributes, memberA, null, null);

                authToken = new OAuth2AuthenticationToken(
                                customOAuth2User,
                                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                                "google");
        }

        @Test
        void 팀_챌린지_진행상황을_조회한다() throws Exception {
                // given
                Member memberB = memberRepository.save(TestFixture.createUniqueMember("userB", "B"));
                ChallengeTeam team = challengeTeamRepository.save(createChallengeTeam(challenge.getId(), 77));

                // Member A: Completed 2 days
                challengeParticipantRepository.save(
                                createTeamParticipant(challenge.getId(), memberA.getId(), team.getId(), 2, false));

                // Member B: Completed 3 days
                ChallengeParticipant participantB = challengeParticipantRepository.save(
                                createTeamParticipant(challenge.getId(), memberB.getId(), team.getId(), 3, true));

                // Daily Results for Member B
                ChallengeDailyResult resultB1 = createChallengeDailyResult(participantB.getId(), LocalDate.now(),
                                ChallengeDailyStatus.COMPLETE);
                ChallengeDailyResult resultB2 = createChallengeDailyResult(participantB.getId(),
                                LocalDate.now().plusDays(1),
                                ChallengeDailyStatus.SHIELD);
                challengeDailyResultRepository.saveAll(List.of(resultB1, resultB2));

                // when & then
                mockMvc.perform(get("/api/v1/challenges/{id}/progress/team", challenge.getId())
                                .with(authentication(authToken)))
                                .andExpect(status().isOk())
                                .andDo(print())
                                .andExpect(jsonPath("$.teamSummary.achievementAverage").value(team.getProgress()))
                                .andExpect(jsonPath("$.members").isArray())
                                .andExpect(jsonPath("$.members[0].nickname").value(memberB.getNickname())) // Sorted by completedDays
                                .andExpect(jsonPath("$.members[0].dailyProgresses").isArray())
                                .andExpect(jsonPath("$.members[0].dailyProgresses[0].status").value("COMPLETE"))
                                .andExpect(jsonPath("$.members[0].dailyProgresses[1].status").value("SHIELD"))

                                .andExpect(jsonPath("$.members[1].nickname").value(memberA.getNickname()))
                                .andDo(print());
        }

        private ChallengeParticipant createTeamParticipant(Long challengeId, Long memberId, Long teamId,
                        int completedDays, boolean isSurvived) {
                return ChallengeParticipant.builder()
                                .challengeId(challengeId)
                                .memberId(memberId)
                                .challengeTeamId(teamId)
                                .completedDays(completedDays)
                                .isSurvived(isSurvived)
                                .shield(0)
                                .build();
        }

        private ChallengeTeam createChallengeTeam(Long challengeId, int progress) {
                return ChallengeTeam.builder()
                                .challengeId(challengeId)
                                .progress(progress)
                                .build();
        }

        private ChallengeDailyResult createChallengeDailyResult(Long participantId, LocalDate date,
                        ChallengeDailyStatus status) {
                return ChallengeDailyResult.builder()
                                .participantId(participantId)
                                .date(date)
                                .status(status)
                                .build();
        }
}
