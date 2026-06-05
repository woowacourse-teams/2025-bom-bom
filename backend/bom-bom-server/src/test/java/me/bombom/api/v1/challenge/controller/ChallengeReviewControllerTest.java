package me.bombom.api.v1.challenge.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.auth.handler.OAuth2LoginSuccessHandler;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeReview;
import me.bombom.api.v1.challenge.domain.ChallengeTeam;
import me.bombom.api.v1.challenge.domain.ChallengeTodoType;
import me.bombom.api.v1.challenge.event.CreateChallengeCommentEvent;
import me.bombom.api.v1.challenge.event.CreateChallengeCommentListener;
import me.bombom.api.v1.challenge.repository.ChallengeDailyResultRepository;
import me.bombom.api.v1.challenge.repository.ChallengeDailyTodoRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeReviewRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTeamRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTodoRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
class ChallengeReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ChallengeReviewRepository challengeReviewRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ChallengeParticipantRepository challengeParticipantRepository;

    @Autowired
    private ChallengeTodoRepository challengeTodoRepository;

    @Autowired
    private ChallengeDailyTodoRepository challengeDailyTodoRepository;

    @Autowired
    private ChallengeDailyResultRepository challengeDailyResultRepository;

    @Autowired
    private ChallengeTeamRepository challengeTeamRepository;

    @Autowired
    private CreateChallengeCommentListener createChallengeCommentListener;

    @Autowired
    private Clock clock;

    @MockitoBean
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    private Member viewer;
    private Member otherMember;
    private OAuth2AuthenticationToken viewerAuth;
    private Long challengeAId;
    private Long challengeBId;
    private Long viewerParticipantId;
    private Long viewerTeamId;

    @BeforeEach
    void setUp() {
        challengeDailyResultRepository.deleteAllInBatch();
        challengeDailyTodoRepository.deleteAllInBatch();
        challengeReviewRepository.deleteAllInBatch();
        challengeParticipantRepository.deleteAllInBatch();
        challengeTeamRepository.deleteAllInBatch();
        challengeTodoRepository.deleteAllInBatch();
        challengeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        viewer = memberRepository.save(TestFixture.createUniqueMember("나밍곰", "viewer-provider"));
        otherMember = memberRepository.save(TestFixture.createUniqueMember("제나", "other-provider"));

        // 프로덕션 Clock 빈(Asia/Seoul) 과 시간대 일치 보장 — CI(UTC) 환경에서 LocalDate.now() 와 차이 방지
        LocalDate today = LocalDate.now(clock);
        // 챌린지A: 오늘이 마지막 날 (REVIEW 가 진행도 TODO 리스트에 노출되는 조건)
        Challenge challengeA = challengeRepository.save(
                TestFixture.createChallenge("챌린지A", today.minusDays(2), today, 3, 1L)
        );
        Challenge challengeB = challengeRepository.save(
                TestFixture.createChallenge("챌린지B", today, today.plusDays(10), 11, 2L)
        );
        challengeAId = challengeA.getId();
        challengeBId = challengeB.getId();

        // TODO 타입 시드 — REVIEW + COMMENT (idempotency 검증용)
        challengeTodoRepository.save(TestFixture.createChallengeTodo(challengeAId, ChallengeTodoType.REVIEW));
        challengeTodoRepository.save(TestFixture.createChallengeTodo(challengeAId, ChallengeTodoType.COMMENT));
        challengeTodoRepository.save(TestFixture.createChallengeTodo(challengeBId, ChallengeTodoType.REVIEW));

        // 팀 + 참여자 (viewer 는 challengeA 참여자, team 포함)
        ChallengeTeam viewerTeam = challengeTeamRepository.save(TestFixture.createChallengeTeam(challengeAId, 0));
        viewerTeamId = viewerTeam.getId();
        ChallengeParticipant savedParticipant = challengeParticipantRepository.save(
                TestFixture.createChallengeParticipantWithTeam(challengeAId, viewer.getId(), viewerTeamId, 0, 0)
        );
        viewerParticipantId = savedParticipant.getId();

        viewerAuth = authOf(viewer);
    }

    @Test
    void 로그인한_사용자는_가시성_정책에_맞는_리뷰_목록을_조회한다() throws Exception {
        // given
        Member anotherMember = saveMember("익명", "another-provider");
        Member hiddenMember = saveMember("숨김", "hidden-provider");
        save(challengeAId, viewer.getId(), "내 비공개", true);
        save(challengeAId, otherMember.getId(), "타인 공개", false);
        save(challengeAId, anotherMember.getId(), "또 다른 타인 공개", false);
        save(challengeAId, hiddenMember.getId(), "타인 비공개", true);
        save(challengeBId, viewer.getId(), "다른 챌린지 본인 공개", false);

        // when // then
        mockMvc.perform(get("/api/v1/challenges/{challengeId}/reviews", challengeAId)
                        .with(authentication(viewerAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[?(@.comment == '타인 비공개')]").isEmpty())
                .andExpect(jsonPath("$.content[?(@.comment == '다른 챌린지 본인 공개')]").isEmpty())
                .andExpect(jsonPath("$.content[?(@.comment == '내 비공개')]").isEmpty())
                .andExpect(jsonPath("$.content[?(@.comment == '타인 공개')]").exists())
                .andExpect(jsonPath("$.content[?(@.comment == '또 다른 타인 공개')]").exists());
    }

    @Test
    void 페이징_파라미터가_적용된다() throws Exception {
        // given
        Member pageMember = saveMember("페이지1", "page-provider-1");
        Member anotherPageMember = saveMember("페이지2", "page-provider-2");
        save(challengeAId, viewer.getId(), "리뷰1", false);
        save(challengeAId, pageMember.getId(), "리뷰2", false);
        save(challengeAId, anotherPageMember.getId(), "리뷰3", false);

        // when // then
        mockMvc.perform(get("/api/v1/challenges/{challengeId}/reviews", challengeAId)
                        .param("page", "0")
                        .param("size", "1")
                        .with(authentication(viewerAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void 비인증_상태로_리뷰_목록을_조회하면_401_을_반환한다() throws Exception {
        mockMvc.perform(get("/api/v1/challenges/{challengeId}/reviews", challengeAId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyReview_내_리뷰가_존재하면_200_과_본문을_반환한다() throws Exception {
        // given
        ChallengeReview mine = save(challengeAId, viewer.getId(), "내 리뷰", true);

        // when // then
        mockMvc.perform(get("/api/v1/challenges/{challengeId}/reviews/me", challengeAId)
                        .with(authentication(viewerAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(mine.getId()))
                .andExpect(jsonPath("$.nickname").value(viewer.getNickname()))
                .andExpect(jsonPath("$.comment").value("내 리뷰"))
                .andExpect(jsonPath("$.isPrivate").value(true));
    }

    @Test
    void getMyReview_비인증_상태이면_401_을_반환한다() throws Exception {
        mockMvc.perform(get("/api/v1/challenges/{challengeId}/reviews/me", challengeAId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createReview_정상_요청이면_201_과_함께_리뷰가_저장되고_당일_출석이_인정된다() throws Exception {
        String body = "{\"comment\":\"좋았어요\",\"isPrivate\":true}";

        mockMvc.perform(post("/api/v1/challenges/{challengeId}/reviews", challengeAId)
                        .with(authentication(viewerAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        // 리뷰 저장 검증
        assertThat(challengeReviewRepository.findAll()).hasSize(1);
        ChallengeReview saved = challengeReviewRepository.findAll().get(0);
        assertThat(saved.getChallengeId()).isEqualTo(challengeAId);
        assertThat(saved.getMemberId()).isEqualTo(viewer.getId());
        assertThat(saved.getComment()).isEqualTo("좋았어요");
        assertThat(saved.isPrivate()).isTrue();

        // 출석 인정 검증 (이벤트 리스너 트리거 결과)
        Long participantId = challengeParticipantRepository
                .findByChallengeIdAndMemberId(challengeAId, viewer.getId())
                .orElseThrow()
                .getId();
        assertThat(challengeDailyResultRepository.existsByParticipantIdAndDate(participantId, LocalDate.now(clock)))
                .isTrue();
        assertThat(challengeDailyTodoRepository.count()).isEqualTo(1); // REVIEW 타입 daily todo 1건
    }

    @Test
    void createReview_비인증_상태이면_401_을_반환한다() throws Exception {
        String body = "{\"comment\":\"좋았어요\",\"isPrivate\":false}";

        mockMvc.perform(post("/api/v1/challenges/{challengeId}/reviews", challengeAId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createReview_동시_요청이_여러_건이어도_정확히_한_건만_저장되고_나머지는_400_을_반환한다() throws Exception {
        // given
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        AtomicInteger createdCount = new AtomicInteger();
        AtomicInteger badRequestCount = new AtomicInteger();
        String body = "{\"comment\":\"동시 요청\",\"isPrivate\":false}";

        // when
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    ready.countDown();
                    start.await();
                    int status = mockMvc.perform(post("/api/v1/challenges/{challengeId}/reviews", challengeAId)
                                    .with(authentication(viewerAuth))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(body))
                            .andReturn()
                            .getResponse()
                            .getStatus();
                    if (status == 201) createdCount.incrementAndGet();
                    else if (status == 400) badRequestCount.incrementAndGet();
                } catch (Exception ignored) {
                } finally {
                    done.countDown();
                }
            });
        }
        ready.await();
        start.countDown();
        done.await(10, TimeUnit.SECONDS);
        executor.shutdownNow();

        // then
        assertThat(createdCount.get()).isEqualTo(1);
        assertThat(badRequestCount.get()).isEqualTo(threadCount - 1);
        assertThat(challengeReviewRepository.findAll()).hasSize(1);
    }

    @Test
    void updateReview_정상_요청이면_204_와_함께_리뷰가_갱신된다() throws Exception {
        ChallengeReview mine = save(challengeAId, viewer.getId(), "원본", false);
        String body = "{\"comment\":\"수정됨\",\"isPrivate\":true}";

        mockMvc.perform(put("/api/v1/challenges/{challengeId}/reviews/{reviewId}", challengeAId, mine.getId())
                        .with(authentication(viewerAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());

        ChallengeReview updated = challengeReviewRepository.findById(mine.getId()).orElseThrow();
        assertThat(updated.getComment()).isEqualTo("수정됨");
        assertThat(updated.isPrivate()).isTrue();
    }

    @Test
    void updateReview_비인증_상태이면_401_을_반환한다() throws Exception {
        ChallengeReview mine = save(challengeAId, viewer.getId(), "원본", false);
        String body = "{\"comment\":\"수정됨\",\"isPrivate\":true}";

        mockMvc.perform(put("/api/v1/challenges/{challengeId}/reviews/{reviewId}", challengeAId, mine.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 리뷰_작성_시_진행도의_REVIEW_TODO가_미완료에서_완료로_변경된다() throws Exception {
        // given — 작성 전 진행도 조회
        mockMvc.perform(get("/api/v1/challenges/{id}/progress/me", challengeAId)
                        .with(authentication(viewerAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todayTodos[?(@.challengeTodoType == 'REVIEW' && @.challengeTodoStatus == 'INCOMPLETE')]").exists())
                .andExpect(jsonPath("$.todayTodos[?(@.challengeTodoType == 'REVIEW' && @.challengeTodoStatus == 'COMPLETE')]").doesNotExist());

        // when — 리뷰 작성
        String body = "{\"comment\":\"잘 했어요\",\"isPrivate\":false}";
        mockMvc.perform(post("/api/v1/challenges/{challengeId}/reviews", challengeAId)
                        .with(authentication(viewerAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        // then — 작성 후 진행도 조회: REVIEW 상태가 COMPLETE 로 변경
        mockMvc.perform(get("/api/v1/challenges/{id}/progress/me", challengeAId)
                        .with(authentication(viewerAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todayTodos[?(@.challengeTodoType == 'REVIEW' && @.challengeTodoStatus == 'COMPLETE')]").exists())
                .andExpect(jsonPath("$.todayTodos[?(@.challengeTodoType == 'REVIEW' && @.challengeTodoStatus == 'INCOMPLETE')]").doesNotExist());
    }

    @Test
    void 리뷰_작성만으로_출석이_인정된다() throws Exception {
        // when
        String body = "{\"comment\":\"리뷰만 작성\",\"isPrivate\":false}";
        mockMvc.perform(post("/api/v1/challenges/{challengeId}/reviews", challengeAId)
                        .with(authentication(viewerAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        // then — 출석 인정
        assertThat(challengeDailyResultRepository.existsByParticipantIdAndDate(viewerParticipantId, LocalDate.now(clock)))
                .isTrue();
        assertThat(challengeParticipantRepository.findById(viewerParticipantId).orElseThrow().getCompletedDays())
                .isEqualTo(1);
    }

    @Test
    void 코멘트와_리뷰_둘_다_작성해도_출석은_단_1회만_인정된다() throws Exception {
        // given — 코멘트 작성 이벤트 직접 발행 (코멘트 API 의존성 회피)
        createChallengeCommentListener.on(new CreateChallengeCommentEvent(viewerParticipantId));
        long dailyResultCountAfterComment = challengeDailyResultRepository.count();
        int completedDaysAfterComment = challengeParticipantRepository.findById(viewerParticipantId)
                .orElseThrow().getCompletedDays();
        assertThat(dailyResultCountAfterComment).isEqualTo(1);
        assertThat(completedDaysAfterComment).isEqualTo(1);

        // when — 같은 날 리뷰 추가 작성
        String body = "{\"comment\":\"리뷰도 작성\",\"isPrivate\":false}";
        mockMvc.perform(post("/api/v1/challenges/{challengeId}/reviews", challengeAId)
                        .with(authentication(viewerAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        // then — 출석은 1회만 인정 (idempotent)
        assertThat(challengeDailyResultRepository.count()).isEqualTo(1);
        assertThat(challengeParticipantRepository.findById(viewerParticipantId).orElseThrow().getCompletedDays())
                .isEqualTo(1);
    }

    @Test
    void 리뷰도_코멘트도_미작성이면_출석이_인정되지_않는다() {
        // when // then — 아무 행위도 하지 않음 → 출석 데이터 0건
        assertThat(challengeDailyResultRepository.existsByParticipantIdAndDate(viewerParticipantId, LocalDate.now(clock)))
                .isFalse();
        assertThat(challengeParticipantRepository.findById(viewerParticipantId).orElseThrow().getCompletedDays())
                .isZero();
    }

    private ChallengeReview save(Long challengeId, Long memberId, String comment, boolean isPrivate) {
        return challengeReviewRepository.save(
                ChallengeReview.builder()
                        .challengeId(challengeId)
                        .memberId(memberId)
                        .comment(comment)
                        .isPrivate(isPrivate)
                        .build()
        );
    }

    private OAuth2AuthenticationToken authOf(Member member) {
        Map<String, Object> attributes = Map.of(
                "id", member.getId().toString(),
                "email", member.getEmail(),
                "name", member.getNickname()
        );
        CustomOAuth2User principal = new CustomOAuth2User(attributes, member, null, null);
        return new OAuth2AuthenticationToken(principal, principal.getAuthorities(), "registrationId");
    }

    private Member saveMember(String nickname, String providerId) {
        return memberRepository.save(TestFixture.createUniqueMember(nickname, providerId));
    }
}
