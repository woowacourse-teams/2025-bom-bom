package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.LocalDate;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeReview;
import me.bombom.api.v1.challenge.dto.request.CreateChallengeReviewRequest;
import me.bombom.api.v1.challenge.dto.request.UpdateChallengeReviewRequest;
import me.bombom.api.v1.challenge.dto.response.ChallengeReviewResponse;
import me.bombom.api.v1.challenge.dto.response.MyChallengeReviewResponse;
import me.bombom.api.v1.challenge.event.CreateChallengeReviewEvent;
import me.bombom.api.v1.challenge.event.CreateChallengeReviewListener;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeReviewRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@IntegrationTest
class ChallengeReviewServiceTest {

    private static final Long NON_EXISTENT_CHALLENGE_ID = 999_999L;
    private static final Long NON_EXISTENT_REVIEW_ID = 999_999L;
    private static final Long WITHDRAWN_MEMBER_ID = 999_999L;

    @Autowired
    private ChallengeReviewService challengeReviewService;

    @MockitoSpyBean
    private ChallengeReviewRepository challengeReviewRepository;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ChallengeParticipantRepository challengeParticipantRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private Clock clock;

    @MockitoBean
    private CreateChallengeReviewListener createChallengeReviewListener;

    private Member viewer;
    private Member other;

    @BeforeEach
    void setUp() {
        challengeReviewRepository.deleteAllInBatch();
        challengeParticipantRepository.deleteAllInBatch();
        challengeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        viewer = memberRepository.save(TestFixture.createUniqueMember("나밍곰", "viewer-provider"));
        other = memberRepository.save(TestFixture.createUniqueMember("제나", "other-provider"));
    }

    @Test
    void 챌린지가_존재하지_않으면_404_예외를_던진다() {
        // when // then
        assertThatThrownBy(() -> challengeReviewService.getReviews(
                NON_EXISTENT_CHALLENGE_ID, viewer.getId(), PageRequest.of(0, 20)))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 조회된_리뷰는_nickname이_매핑된다() {
        // given
        Challenge challenge = challengeRepository.save(ongoingChallenge());
        saveReview(challenge.getId(), other.getId(), "타인 공개", false);

        // when
        Page<ChallengeReviewResponse> result = challengeReviewService.getReviews(
                challenge.getId(), viewer.getId(), PageRequest.of(0, 20)
        );

        // then
        assertThat(result.getContent())
                .extracting(ChallengeReviewResponse::nickname)
                .containsExactly("제나");
    }

    @Test
    void Member의_nickname이_null이면_탈퇴한_사용자로_표시된다() {
        // given — Member 가 없는 리뷰는 LEFT JOIN 결과 nickname 이 null 이 된다
        Challenge challenge = challengeRepository.save(ongoingChallenge());
        saveReview(challenge.getId(), WITHDRAWN_MEMBER_ID, "탈퇴자 리뷰", false);

        // when
        Page<ChallengeReviewResponse> result = challengeReviewService.getReviews(
                challenge.getId(), viewer.getId(), PageRequest.of(0, 20)
        );

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).nickname()).isEqualTo("탈퇴한 사용자");
    }

    @Test
    void getMyReview_챌린지가_존재하지_않으면_404_예외를_던진다() {
        // when // then
        assertThatThrownBy(() -> challengeReviewService.getMyReview(NON_EXISTENT_CHALLENGE_ID, viewer))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void getMyReview_내_리뷰가_존재하지_않으면_404_예외를_던진다() {
        // given
        Challenge challenge = challengeRepository.save(ongoingChallenge());

        // when // then
        assertThatThrownBy(() -> challengeReviewService.getMyReview(challenge.getId(), viewer))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void getMyReview_내_리뷰가_존재하면_MyChallengeReviewResponse_로_매핑된다() {
        // given
        Challenge challenge = challengeRepository.save(ongoingChallenge());
        ChallengeReview review = saveReview(challenge.getId(), viewer.getId(), "내 리뷰", true);

        // when
        MyChallengeReviewResponse result = challengeReviewService.getMyReview(challenge.getId(), viewer);

        // then
        assertThat(result.reviewId()).isEqualTo(review.getId());
        assertThat(result.nickname()).isEqualTo("나밍곰");
        assertThat(result.comment()).isEqualTo("내 리뷰");
        assertThat(result.isPrivate()).isTrue();
    }

    @Test
    void createReview_챌린지가_존재하지_않으면_404_예외를_던지고_저장되지_않으며_이벤트도_발행되지_않는다() {
        // when // then
        assertThatThrownBy(() -> challengeReviewService.createReview(
                NON_EXISTENT_CHALLENGE_ID, viewer, createRequest()))
                .isInstanceOf(CIllegalArgumentException.class);
        assertThat(challengeReviewRepository.count()).isZero();
        verify(createChallengeReviewListener, never()).on(any(CreateChallengeReviewEvent.class));
    }

    @Test
    void createReview_챌린지_시작_전이면_400_예외를_던지고_저장되지_않으며_이벤트도_발행되지_않는다() {
        // given
        Challenge challenge = challengeRepository.save(notStartedChallenge());

        // when // then
        assertThatThrownBy(() -> challengeReviewService.createReview(challenge.getId(), viewer, createRequest()))
                .isInstanceOf(CIllegalArgumentException.class);
        assertThat(challengeReviewRepository.count()).isZero();
        verify(createChallengeReviewListener, never()).on(any(CreateChallengeReviewEvent.class));
    }

    @Test
    void createReview_챌린지_진행_중_시작_후_종료_전_이면_400_예외를_던지고_저장되지_않으며_이벤트도_발행되지_않는다() {
        // given
        Challenge challenge = challengeRepository.save(ongoingChallenge());

        // when // then
        assertThatThrownBy(() -> challengeReviewService.createReview(challenge.getId(), viewer, createRequest()))
                .isInstanceOf(CIllegalArgumentException.class);
        assertThat(challengeReviewRepository.count()).isZero();
        verify(createChallengeReviewListener, never()).on(any(CreateChallengeReviewEvent.class));
    }

    @Test
    void createReview_챌린지_종료_후에는_리뷰는_저장되지만_이벤트가_발행되지_않는다() {
        // given
        Challenge challenge = challengeRepository.save(endedChallenge());
        challengeParticipantRepository.save(participantOf(challenge.getId(), viewer.getId()));

        // when
        challengeReviewService.createReview(challenge.getId(), viewer, createRequest());

        // then
        assertThat(challengeReviewRepository.findByChallengeIdAndMemberId(challenge.getId(), viewer.getId()))
                .isPresent();
        verify(createChallengeReviewListener, never()).on(any(CreateChallengeReviewEvent.class));
    }

    @Test
    void createReview_이미_본인이_작성한_리뷰가_있으면_400_예외를_던지고_저장되지_않으며_이벤트도_발행되지_않는다() {
        // given
        Challenge challenge = challengeRepository.save(lastDayChallenge());
        saveReview(challenge.getId(), viewer.getId(), "기존 리뷰", false);

        // when // then
        assertThatThrownBy(() -> challengeReviewService.createReview(challenge.getId(), viewer, createRequest()))
                .isInstanceOf(CIllegalArgumentException.class);
        // 기존 1건만 존재하고 추가 저장은 일어나지 않았음
        assertThat(challengeReviewRepository.count()).isEqualTo(1);
        verify(createChallengeReviewListener, never()).on(any(CreateChallengeReviewEvent.class));
    }

    @Test
    void createReview_정상_케이스에서_요청값으로_ChallengeReview가_저장된다() {
        // given
        Challenge challenge = challengeRepository.save(lastDayChallenge());
        challengeParticipantRepository.save(participantOf(challenge.getId(), viewer.getId()));

        // when
        challengeReviewService.createReview(challenge.getId(), viewer,
                new CreateChallengeReviewRequest("좋았어요", true));

        // then
        ChallengeReview saved = challengeReviewRepository
                .findByChallengeIdAndMemberId(challenge.getId(), viewer.getId())
                .orElseThrow();
        assertThat(saved.getChallengeId()).isEqualTo(challenge.getId());
        assertThat(saved.getMemberId()).isEqualTo(viewer.getId());
        assertThat(saved.getComment()).isEqualTo("좋았어요");
        assertThat(saved.isPrivate()).isTrue();
    }

    @Test
    void createReview_save_시점에_unique_제약_위반이_발생하면_DUPLICATED_DATA로_변환되고_이벤트도_발행되지_않는다() {
        // given — 동시 작성 레이스 시나리오: pre-check 를 우회해 DB unique 제약을 직접 트리거
        Challenge challenge = challengeRepository.save(lastDayChallenge());
        challengeParticipantRepository.save(participantOf(challenge.getId(), viewer.getId()));
        saveReview(challenge.getId(), viewer.getId(), "기존 리뷰", false);

        doReturn(false).when(challengeReviewRepository)
                .existsByChallengeIdAndMemberId(challenge.getId(), viewer.getId());

        // when // then
        assertThatThrownBy(() -> challengeReviewService.createReview(challenge.getId(), viewer, createRequest()))
                .isInstanceOf(CIllegalArgumentException.class);
        verify(createChallengeReviewListener, never()).on(any(CreateChallengeReviewEvent.class));
    }

    @Test
    void createReview_비참여자이면_404_예외를_던지고_저장되지_않는다_IDOR_방어() {
        // given — participant 가 없는 상태
        Challenge challenge = challengeRepository.save(lastDayChallenge());

        // when // then
        assertThatThrownBy(() -> challengeReviewService.createReview(challenge.getId(), viewer, createRequest()))
                .isInstanceOf(CIllegalArgumentException.class);
        assertThat(challengeReviewRepository.count()).isZero();
        verify(createChallengeReviewListener, never()).on(any(CreateChallengeReviewEvent.class));
    }

    @Test
    void createReview_정상_케이스에서_CreateChallengeReviewEvent_가_오늘_날짜로_발행된다() {
        // given
        Challenge challenge = challengeRepository.save(lastDayChallenge());
        ChallengeParticipant participant = challengeParticipantRepository
                .save(participantOf(challenge.getId(), viewer.getId()));

        // when
        challengeReviewService.createReview(challenge.getId(), viewer, createRequest());

        // then
        ArgumentCaptor<CreateChallengeReviewEvent> captor = ArgumentCaptor.forClass(CreateChallengeReviewEvent.class);
        verify(createChallengeReviewListener).on(captor.capture());
        assertThat(captor.getValue().participantId()).isEqualTo(participant.getId());
        assertThat(captor.getValue().reviewDate()).isEqualTo(LocalDate.now(clock));
    }

    @Test
    void updateReview_리뷰가_존재하지_않으면_404_예외를_던진다() {
        // given
        Challenge challenge = challengeRepository.save(ongoingChallenge());

        // when // then
        assertThatThrownBy(() -> challengeReviewService.updateReview(
                challenge.getId(), NON_EXISTENT_REVIEW_ID, viewer, updateRequest()))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void updateReview_path_challengeId와_review의_challengeId가_불일치하면_404_예외를_던진다() {
        // given
        Challenge challengeA = challengeRepository.save(ongoingChallenge());
        Challenge challengeB = challengeRepository.save(ongoingChallenge());
        ChallengeReview review = saveReview(challengeB.getId(), viewer.getId(), "원본 코멘트", false);

        // when // then
        assertThatThrownBy(() -> challengeReviewService.updateReview(
                challengeA.getId(), review.getId(), viewer, updateRequest()))
                .isInstanceOf(CIllegalArgumentException.class);
        ChallengeReview reloaded = challengeReviewRepository.findById(review.getId()).orElseThrow();
        assertThat(reloaded.getComment()).isEqualTo("원본 코멘트");
    }

    @Test
    void updateReview_본인_리뷰가_아니면_404_예외를_던진다() {
        // given
        Challenge challenge = challengeRepository.save(ongoingChallenge());
        ChallengeReview review = saveReview(challenge.getId(), other.getId(), "원본 코멘트", false);

        // when // then
        assertThatThrownBy(() -> challengeReviewService.updateReview(
                challenge.getId(), review.getId(), viewer, updateRequest()))
                .isInstanceOf(CIllegalArgumentException.class);
        ChallengeReview reloaded = challengeReviewRepository.findById(review.getId()).orElseThrow();
        assertThat(reloaded.getComment()).isEqualTo("원본 코멘트");
    }

    @Test
    void updateReview_정상_케이스에서_엔티티의_상태가_갱신된다() {
        // given
        Challenge challenge = challengeRepository.save(ongoingChallenge());
        ChallengeReview review = saveReview(challenge.getId(), viewer.getId(), "원본 코멘트", false);

        // when
        challengeReviewService.updateReview(
                challenge.getId(), review.getId(), viewer,
                new UpdateChallengeReviewRequest("수정된 코멘트", true)
        );

        // then
        ChallengeReview reloaded = challengeReviewRepository.findById(review.getId()).orElseThrow();
        assertThat(reloaded.getComment()).isEqualTo("수정된 코멘트");
        assertThat(reloaded.isPrivate()).isTrue();
    }

    private Challenge ongoingChallenge() {
        LocalDate today = LocalDate.now(clock);
        return TestFixture.createChallenge("진행 중 챌린지",
                today.minusDays(3), today.plusDays(5), 9, 1L);
    }

    private Challenge lastDayChallenge() {
        LocalDate today = LocalDate.now(clock);
        return TestFixture.createChallenge("마지막 날 챌린지",
                today.minusDays(7), today, 8, 1L);
    }

    private Challenge notStartedChallenge() {
        LocalDate today = LocalDate.now(clock);
        return TestFixture.createChallenge("시작 전 챌린지",
                today.plusDays(5), today.plusDays(15), 11, 1L);
    }

    private Challenge endedChallenge() {
        LocalDate today = LocalDate.now(clock);
        return TestFixture.createChallenge("종료된 챌린지",
                today.minusDays(30), today.minusDays(10), 21, 1L);
    }

    private ChallengeParticipant participantOf(Long challengeId, Long memberId) {
        return ChallengeParticipant.builder()
                .challengeId(challengeId)
                .memberId(memberId)
                .completedDays(0)
                .isSurvived(true)
                .shield(0)
                .streak(0)
                .build();
    }

    private ChallengeReview saveReview(Long challengeId, Long memberId, String comment, boolean isPrivate) {
        return challengeReviewRepository.save(
                ChallengeReview.builder()
                        .challengeId(challengeId)
                        .memberId(memberId)
                        .comment(comment)
                        .isPrivate(isPrivate)
                        .build()
        );
    }

    private CreateChallengeReviewRequest createRequest() {
        return new CreateChallengeReviewRequest("리뷰 코멘트", false);
    }

    private UpdateChallengeReviewRequest updateRequest() {
        return new UpdateChallengeReviewRequest("수정된 코멘트", true);
    }
}
