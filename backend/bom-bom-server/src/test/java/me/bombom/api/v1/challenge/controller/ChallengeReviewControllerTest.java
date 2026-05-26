package me.bombom.api.v1.challenge.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import me.bombom.api.v1.challenge.domain.ChallengeReview;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeReviewRepository;
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

    @MockitoBean
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    private Member viewer;
    private Member otherMember;
    private OAuth2AuthenticationToken viewerAuth;
    private Long challengeAId;
    private Long challengeBId;

    @BeforeEach
    void setUp() {
        challengeReviewRepository.deleteAllInBatch();
        challengeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        viewer = memberRepository.save(TestFixture.createUniqueMember("나밍곰", "viewer-provider"));
        otherMember = memberRepository.save(TestFixture.createUniqueMember("제나", "other-provider"));

        Challenge challengeA = challengeRepository.save(
                TestFixture.createChallenge("챌린지A", LocalDate.now(), LocalDate.now().plusDays(10), 11, 1L)
        );
        Challenge challengeB = challengeRepository.save(
                TestFixture.createChallenge("챌린지B", LocalDate.now(), LocalDate.now().plusDays(10), 11, 2L)
        );
        challengeAId = challengeA.getId();
        challengeBId = challengeB.getId();

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
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content[?(@.comment == '타인 비공개')]").isEmpty())
                .andExpect(jsonPath("$.content[?(@.comment == '다른 챌린지 본인 공개')]").isEmpty())
                .andExpect(jsonPath("$.content[?(@.isMyReview == true && @.comment == '내 비공개')]").exists())
                .andExpect(jsonPath("$.content[?(@.isMyReview == false && @.comment == '타인 공개')]").exists());
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
                        .param("size", "2")
                        .with(authentication(viewerAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void 존재하지_않는_챌린지_조회_시_404_를_반환한다() throws Exception {
        // given
        long missingChallengeId = 999_999L;

        // when // then
        mockMvc.perform(get("/api/v1/challenges/{challengeId}/reviews", missingChallengeId)
                        .with(authentication(viewerAuth)))
                .andExpect(status().isNotFound());
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
                .andExpect(jsonPath("$.isPrivate").value(true))
                .andExpect(jsonPath("$.isMyReview").doesNotExist());
    }

    @Test
    void getMyReview_내_리뷰가_없으면_404_를_반환한다() throws Exception {
        // given
        save(challengeAId, otherMember.getId(), "타인 공개", false);

        // when // then
        mockMvc.perform(get("/api/v1/challenges/{challengeId}/reviews/me", challengeAId)
                        .with(authentication(viewerAuth)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMyReview_존재하지_않는_챌린지면_404_를_반환한다() throws Exception {
        // given
        long missingChallengeId = 999_999L;

        // when // then
        mockMvc.perform(get("/api/v1/challenges/{challengeId}/reviews/me", missingChallengeId)
                        .with(authentication(viewerAuth)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMyReview_다른_챌린지의_내_리뷰만_있을_때_404_를_반환한다() throws Exception {
        // given
        save(challengeBId, viewer.getId(), "다른 챌린지 본인 리뷰", false);

        // when // then
        mockMvc.perform(get("/api/v1/challenges/{challengeId}/reviews/me", challengeAId)
                        .with(authentication(viewerAuth)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMyReview_비인증_상태이면_401_을_반환한다() throws Exception {
        mockMvc.perform(get("/api/v1/challenges/{challengeId}/reviews/me", challengeAId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createReview_정상_요청이면_201_과_함께_리뷰가_저장된다() throws Exception {
        String body = "{\"comment\":\"좋았어요\",\"isPrivate\":true}";

        mockMvc.perform(post("/api/v1/challenges/{challengeId}/reviews", challengeAId)
                        .with(authentication(viewerAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        assertThat(challengeReviewRepository.findAll()).hasSize(1);
        ChallengeReview saved = challengeReviewRepository.findAll().get(0);
        assertThat(saved.getChallengeId()).isEqualTo(challengeAId);
        assertThat(saved.getMemberId()).isEqualTo(viewer.getId());
        assertThat(saved.getComment()).isEqualTo("좋았어요");
        assertThat(saved.isPrivate()).isTrue();
    }

    @Test
    void createReview_이미_본인이_작성한_리뷰가_있으면_400_을_반환한다() throws Exception {
        save(challengeAId, viewer.getId(), "기존 리뷰", false);
        String body = "{\"comment\":\"중복 시도\",\"isPrivate\":false}";

        mockMvc.perform(post("/api/v1/challenges/{challengeId}/reviews", challengeAId)
                        .with(authentication(viewerAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        assertThat(challengeReviewRepository.findAll()).hasSize(1);
    }

    @Test
    void createReview_존재하지_않는_챌린지면_404_를_반환한다() throws Exception {
        long missingChallengeId = 999_999L;
        String body = "{\"comment\":\"좋았어요\",\"isPrivate\":false}";

        mockMvc.perform(post("/api/v1/challenges/{challengeId}/reviews", missingChallengeId)
                        .with(authentication(viewerAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
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
    void createReview_comment_가_빈_문자열이면_400_을_반환한다() throws Exception {
        String body = "{\"comment\":\"\",\"isPrivate\":false}";

        mockMvc.perform(post("/api/v1/challenges/{challengeId}/reviews", challengeAId)
                        .with(authentication(viewerAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        assertThat(challengeReviewRepository.findAll()).isEmpty();
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
    void updateReview_리뷰가_존재하지_않으면_404_를_반환한다() throws Exception {
        long missingReviewId = 999_999L;
        String body = "{\"comment\":\"수정됨\",\"isPrivate\":true}";

        mockMvc.perform(put("/api/v1/challenges/{challengeId}/reviews/{reviewId}", challengeAId, missingReviewId)
                        .with(authentication(viewerAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateReview_path_challengeId_와_review의_challengeId_가_불일치하면_404_를_반환한다() throws Exception {
        ChallengeReview reviewInChallengeB = save(challengeBId, viewer.getId(), "원본", false);
        String body = "{\"comment\":\"수정됨\",\"isPrivate\":true}";

        mockMvc.perform(put("/api/v1/challenges/{challengeId}/reviews/{reviewId}", challengeAId, reviewInChallengeB.getId())
                        .with(authentication(viewerAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());

        ChallengeReview untouched = challengeReviewRepository.findById(reviewInChallengeB.getId()).orElseThrow();
        assertThat(untouched.getComment()).isEqualTo("원본");
    }

    @Test
    void updateReview_본인_리뷰가_아니면_404_를_반환한다_IDOR_방어() throws Exception {
        ChallengeReview othersReview = save(challengeAId, otherMember.getId(), "타인 원본", false);
        String body = "{\"comment\":\"가로채기\",\"isPrivate\":true}";

        mockMvc.perform(put("/api/v1/challenges/{challengeId}/reviews/{reviewId}", challengeAId, othersReview.getId())
                        .with(authentication(viewerAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());

        ChallengeReview untouched = challengeReviewRepository.findById(othersReview.getId()).orElseThrow();
        assertThat(untouched.getComment()).isEqualTo("타인 원본");
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
    void updateReview_comment가_빈_문자열이면_400_을_반환한다() throws Exception {
        ChallengeReview mine = save(challengeAId, viewer.getId(), "원본", false);
        String body = "{\"comment\":\"\",\"isPrivate\":true}";

        mockMvc.perform(put("/api/v1/challenges/{challengeId}/reviews/{reviewId}", challengeAId, mine.getId())
                        .with(authentication(viewerAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        ChallengeReview untouched = challengeReviewRepository.findById(mine.getId()).orElseThrow();
        assertThat(untouched.getComment()).isEqualTo("원본");
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
