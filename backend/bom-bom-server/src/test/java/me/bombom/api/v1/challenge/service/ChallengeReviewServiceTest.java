package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeReview;
import me.bombom.api.v1.challenge.dto.request.CreateChallengeReviewRequest;
import me.bombom.api.v1.challenge.dto.request.UpdateChallengeReviewRequest;
import me.bombom.api.v1.challenge.dto.response.ChallengeReviewListItem;
import me.bombom.api.v1.challenge.dto.response.ChallengeReviewResponse;
import me.bombom.api.v1.challenge.dto.response.MyChallengeReviewResponse;
import me.bombom.api.v1.challenge.event.CreateChallengeReviewEvent;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeReviewRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.enums.Gender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ChallengeReviewServiceTest {

    private static final Long CHALLENGE_ID = 100L;
    private static final Long VIEWER_MEMBER_ID = 1L;
    private static final Long OTHER_MEMBER_ID = 2L;
    private static final Long REVIEW_ID = 42L;

    @Mock
    private ChallengeReviewRepository challengeReviewRepository;

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private ChallengeParticipantRepository challengeParticipantRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @org.mockito.Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-05-27T12:00:00Z"), ZoneId.of("Asia/Seoul"));

    @InjectMocks
    private ChallengeReviewService challengeReviewService;

    @Test
    void 챌린지가_존재하지_않으면_404_예외를_던진다() {
        // given
        given(challengeRepository.existsById(CHALLENGE_ID)).willReturn(false);

        // when // then
        assertThatThrownBy(() -> challengeReviewService.getReviews(CHALLENGE_ID, VIEWER_MEMBER_ID, PageRequest.of(0, 20)))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 본인_리뷰는_isMyReview가_true_타인_리뷰는_false로_매핑된다() {
        // given
        given(challengeRepository.existsById(CHALLENGE_ID)).willReturn(true);
        givenReviews(List.of(
                item(10L, "나밍곰", VIEWER_MEMBER_ID),
                item(20L, "제나", OTHER_MEMBER_ID)
        ));

        // when
        Page<ChallengeReviewResponse> result = challengeReviewService.getReviews(
                CHALLENGE_ID, VIEWER_MEMBER_ID, PageRequest.of(0, 20)
        );

        // then
        assertThat(result.getContent())
                .extracting(ChallengeReviewResponse::isMyReview)
                .containsExactly(true, false);
        assertThat(result.getContent())
                .extracting(ChallengeReviewResponse::nickname)
                .containsExactly("나밍곰", "제나");
    }

    @Test
    void Member의_nickname이_null이면_탈퇴한_사용자로_표시된다() {
        // given
        given(challengeRepository.existsById(CHALLENGE_ID)).willReturn(true);
        givenReviews(List.of(item(30L, null, OTHER_MEMBER_ID)));

        // when
        Page<ChallengeReviewResponse> result = challengeReviewService.getReviews(
                CHALLENGE_ID, VIEWER_MEMBER_ID, PageRequest.of(0, 20)
        );

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).nickname()).isEqualTo("탈퇴한 사용자");
        assertThat(result.getContent().get(0).isMyReview()).isFalse();
    }

    @Test
    void getMyReview_챌린지가_존재하지_않으면_404_예외를_던진다() {
        // given
        given(challengeRepository.existsById(CHALLENGE_ID)).willReturn(false);

        // when // then
        assertThatThrownBy(() -> challengeReviewService.getMyReview(CHALLENGE_ID, viewer()))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void getMyReview_내_리뷰가_존재하지_않으면_404_예외를_던진다() {
        // given
        given(challengeRepository.existsById(CHALLENGE_ID)).willReturn(true);
        given(challengeReviewRepository.findByChallengeIdAndMemberId(CHALLENGE_ID, VIEWER_MEMBER_ID))
                .willReturn(Optional.empty());

        // when // then
        assertThatThrownBy(() -> challengeReviewService.getMyReview(CHALLENGE_ID, viewer()))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void getMyReview_내_리뷰가_존재하면_MyChallengeReviewResponse_로_매핑된다() {
        // given
        given(challengeRepository.existsById(CHALLENGE_ID)).willReturn(true);
        ChallengeReview review = ChallengeReview.builder()
                .id(42L)
                .challengeId(CHALLENGE_ID)
                .memberId(VIEWER_MEMBER_ID)
                .comment("내 리뷰")
                .isPrivate(true)
                .build();
        given(challengeReviewRepository.findByChallengeIdAndMemberId(CHALLENGE_ID, VIEWER_MEMBER_ID))
                .willReturn(Optional.of(review));

        // when
        MyChallengeReviewResponse result = challengeReviewService.getMyReview(CHALLENGE_ID, viewer());

        // then
        assertThat(result.reviewId()).isEqualTo(42L);
        assertThat(result.nickname()).isEqualTo("나밍곰");
        assertThat(result.comment()).isEqualTo("내 리뷰");
        assertThat(result.isPrivate()).isTrue();
    }

    @Test
    void createReview_챌린지가_존재하지_않으면_404_예외를_던지고_저장되지_않으며_이벤트도_발행되지_않는다() {
        // given
        given(challengeRepository.findById(CHALLENGE_ID)).willReturn(Optional.empty());

        // when // then
        assertThatThrownBy(() -> challengeReviewService.createReview(CHALLENGE_ID, viewer(), createRequest()))
                .isInstanceOf(CIllegalArgumentException.class);
        verify(challengeReviewRepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(any(CreateChallengeReviewEvent.class));
    }

    @Test
    void createReview_챌린지_시작_전이면_400_예외를_던지고_저장되지_않으며_이벤트도_발행되지_않는다() {
        // given
        given(challengeRepository.findById(CHALLENGE_ID)).willReturn(Optional.of(notStartedChallenge()));

        // when // then
        assertThatThrownBy(() -> challengeReviewService.createReview(CHALLENGE_ID, viewer(), createRequest()))
                .isInstanceOf(CIllegalArgumentException.class);
        verify(challengeReviewRepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(any(CreateChallengeReviewEvent.class));
    }

    @Test
    void createReview_챌린지_종료_후에는_리뷰는_저장되지만_이벤트가_발행되지_않는다() {
        // given
        given(challengeRepository.findById(CHALLENGE_ID)).willReturn(Optional.of(endedChallenge()));
        given(challengeReviewRepository.existsByChallengeIdAndMemberId(CHALLENGE_ID, VIEWER_MEMBER_ID))
                .willReturn(false);
        given(challengeParticipantRepository.findByChallengeIdAndMemberId(CHALLENGE_ID, VIEWER_MEMBER_ID))
                .willReturn(Optional.of(participant(99L)));

        // when
        challengeReviewService.createReview(CHALLENGE_ID, viewer(), createRequest());

        // then
        verify(challengeReviewRepository).save(any(ChallengeReview.class));
        verify(applicationEventPublisher, never()).publishEvent(any(CreateChallengeReviewEvent.class));
    }

    @Test
    void createReview_이미_본인이_작성한_리뷰가_있으면_400_예외를_던지고_저장되지_않으며_이벤트도_발행되지_않는다() {
        // given
        given(challengeRepository.findById(CHALLENGE_ID)).willReturn(Optional.of(ongoingChallenge()));
        given(challengeReviewRepository.existsByChallengeIdAndMemberId(CHALLENGE_ID, VIEWER_MEMBER_ID))
                .willReturn(true);

        // when // then
        assertThatThrownBy(() -> challengeReviewService.createReview(CHALLENGE_ID, viewer(), createRequest()))
                .isInstanceOf(CIllegalArgumentException.class);
        verify(challengeReviewRepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(any(CreateChallengeReviewEvent.class));
    }

    @Test
    void createReview_정상_케이스에서_요청값으로_ChallengeReview가_저장된다() {
        // given
        given(challengeRepository.findById(CHALLENGE_ID)).willReturn(Optional.of(ongoingChallenge()));
        given(challengeReviewRepository.existsByChallengeIdAndMemberId(CHALLENGE_ID, VIEWER_MEMBER_ID))
                .willReturn(false);
        given(challengeParticipantRepository.findByChallengeIdAndMemberId(CHALLENGE_ID, VIEWER_MEMBER_ID))
                .willReturn(Optional.of(participant(99L)));

        // when
        challengeReviewService.createReview(CHALLENGE_ID, viewer(), new CreateChallengeReviewRequest("좋았어요", true));

        // then
        ArgumentCaptor<ChallengeReview> captor = ArgumentCaptor.forClass(ChallengeReview.class);
        verify(challengeReviewRepository).save(captor.capture());
        ChallengeReview saved = captor.getValue();
        assertThat(saved.getChallengeId()).isEqualTo(CHALLENGE_ID);
        assertThat(saved.getMemberId()).isEqualTo(VIEWER_MEMBER_ID);
        assertThat(saved.getComment()).isEqualTo("좋았어요");
        assertThat(saved.isPrivate()).isTrue();
    }

    @Test
    void createReview_save_시점에_unique_제약_위반이_발생하면_DUPLICATED_DATA로_변환되고_이벤트도_발행되지_않는다() {
        // given
        given(challengeRepository.findById(CHALLENGE_ID)).willReturn(Optional.of(ongoingChallenge()));
        given(challengeReviewRepository.existsByChallengeIdAndMemberId(CHALLENGE_ID, VIEWER_MEMBER_ID))
                .willReturn(false);
        given(challengeParticipantRepository.findByChallengeIdAndMemberId(CHALLENGE_ID, VIEWER_MEMBER_ID))
                .willReturn(Optional.of(participant(99L)));
        given(challengeReviewRepository.save(any(ChallengeReview.class)))
                .willThrow(new DataIntegrityViolationException("unique constraint"));

        // when // then
        assertThatThrownBy(() -> challengeReviewService.createReview(CHALLENGE_ID, viewer(), createRequest()))
                .isInstanceOf(CIllegalArgumentException.class);
        verify(applicationEventPublisher, never()).publishEvent(any(CreateChallengeReviewEvent.class));
    }

    @Test
    void updateReview_리뷰가_존재하지_않으면_404_예외를_던진다() {
        // given
        given(challengeReviewRepository.findById(REVIEW_ID)).willReturn(Optional.empty());

        // when // then
        assertThatThrownBy(() -> challengeReviewService.updateReview(CHALLENGE_ID, REVIEW_ID, viewer(), updateRequest()))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void updateReview_path_challengeId와_review의_challengeId가_불일치하면_404_예외를_던진다() {
        // given
        ChallengeReview review = reviewOwnedBy(VIEWER_MEMBER_ID, 999L);
        given(challengeReviewRepository.findById(REVIEW_ID)).willReturn(Optional.of(review));

        // when // then
        assertThatThrownBy(() -> challengeReviewService.updateReview(CHALLENGE_ID, REVIEW_ID, viewer(), updateRequest()))
                .isInstanceOf(CIllegalArgumentException.class);
        assertThat(review.getComment()).isEqualTo("원본 코멘트");
    }

    @Test
    void updateReview_본인_리뷰가_아니면_404_예외를_던진다() {
        // given
        ChallengeReview review = reviewOwnedBy(OTHER_MEMBER_ID, CHALLENGE_ID);
        given(challengeReviewRepository.findById(REVIEW_ID)).willReturn(Optional.of(review));

        // when // then
        assertThatThrownBy(() -> challengeReviewService.updateReview(CHALLENGE_ID, REVIEW_ID, viewer(), updateRequest()))
                .isInstanceOf(CIllegalArgumentException.class);
        assertThat(review.getComment()).isEqualTo("원본 코멘트");
    }

    @Test
    void updateReview_정상_케이스에서_엔티티의_상태가_갱신된다() {
        // given
        ChallengeReview review = reviewOwnedBy(VIEWER_MEMBER_ID, CHALLENGE_ID);
        given(challengeReviewRepository.findById(REVIEW_ID)).willReturn(Optional.of(review));

        // when
        challengeReviewService.updateReview(CHALLENGE_ID, REVIEW_ID, viewer(), new UpdateChallengeReviewRequest("수정된 코멘트", true));

        // then — JPA dirty checking 가정. 도메인 인스턴스 상태 변경 검증으로 충분
        assertThat(review.getComment()).isEqualTo("수정된 코멘트");
        assertThat(review.isPrivate()).isTrue();
    }

    @Test
    void createReview_비참여자이면_404_예외를_던지고_저장되지_않는다_IDOR_방어() {
        // given
        given(challengeRepository.findById(CHALLENGE_ID)).willReturn(Optional.of(ongoingChallenge()));
        given(challengeReviewRepository.existsByChallengeIdAndMemberId(CHALLENGE_ID, VIEWER_MEMBER_ID))
                .willReturn(false);
        given(challengeParticipantRepository.findByChallengeIdAndMemberId(CHALLENGE_ID, VIEWER_MEMBER_ID))
                .willReturn(Optional.empty());

        // when // then
        assertThatThrownBy(() -> challengeReviewService.createReview(CHALLENGE_ID, viewer(), createRequest()))
                .isInstanceOf(CIllegalArgumentException.class);
        verify(challengeReviewRepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(any(CreateChallengeReviewEvent.class));
    }

    @Test
    void createReview_정상_케이스에서_CreateChallengeReviewEvent_가_Clock_의_날짜로_발행된다() {
        // given
        given(challengeRepository.findById(CHALLENGE_ID)).willReturn(Optional.of(ongoingChallenge()));
        given(challengeReviewRepository.existsByChallengeIdAndMemberId(CHALLENGE_ID, VIEWER_MEMBER_ID))
                .willReturn(false);
        given(challengeParticipantRepository.findByChallengeIdAndMemberId(CHALLENGE_ID, VIEWER_MEMBER_ID))
                .willReturn(Optional.of(participant(99L)));

        // when
        challengeReviewService.createReview(CHALLENGE_ID, viewer(), createRequest());

        // then
        ArgumentCaptor<CreateChallengeReviewEvent> captor = ArgumentCaptor.forClass(CreateChallengeReviewEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().participantId()).isEqualTo(99L);
        // Clock 픽스처
        assertThat(captor.getValue().reviewDate()).isEqualTo(LocalDate.of(2026, 5, 27));
    }

    // Clock 픽스처 기준
    private Challenge ongoingChallenge() {
        return challengeWithPeriod(LocalDate.of(2026, 5, 20), LocalDate.of(2026, 6, 5));
    }

    private Challenge notStartedChallenge() {
        return challengeWithPeriod(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 10));
    }

    private Challenge endedChallenge() {
        return challengeWithPeriod(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 5, 10));
    }

    private Challenge challengeWithPeriod(LocalDate start, LocalDate end) {
        Challenge challenge = Challenge.builder()
                .name("테스트 챌린지")
                .generation(1)
                .startDate(start)
                .endDate(end)
                .totalDays((int) java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1)
                .newsletterGroupId(1L)
                .build();
        try {
            var idField = Challenge.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(challenge, CHALLENGE_ID);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        return challenge;
    }

    private ChallengeParticipant participant(Long participantId) {
        ChallengeParticipant p = ChallengeParticipant.builder()
                .challengeId(CHALLENGE_ID)
                .memberId(VIEWER_MEMBER_ID)
                .build();
        try {
            var idField = ChallengeParticipant.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(p, participantId);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        return p;
    }

    private CreateChallengeReviewRequest createRequest() {
        return new CreateChallengeReviewRequest("리뷰 코멘트", false);
    }

    private UpdateChallengeReviewRequest updateRequest() {
        return new UpdateChallengeReviewRequest("수정된 코멘트", true);
    }

    private ChallengeReview reviewOwnedBy(Long ownerMemberId, Long challengeId) {
        return ChallengeReview.builder()
                .id(REVIEW_ID)
                .challengeId(challengeId)
                .memberId(ownerMemberId)
                .comment("원본 코멘트")
                .isPrivate(false)
                .build();
    }

    private void givenReviews(List<ChallengeReviewListItem> items) {
        given(challengeReviewRepository.findVisibleReviews(anyLong(), anyLong(), any(Pageable.class)))
                .willReturn(new PageImpl<>(items, PageRequest.of(0, 20), items.size()));
    }

    private ChallengeReviewListItem item(Long reviewId, String nickname, Long memberId) {
        return new ChallengeReviewListItem(reviewId, nickname, "리뷰 코멘트", false, memberId);
    }

    private Member viewer() {
        return Member.builder()
                .id(VIEWER_MEMBER_ID)
                .provider("kakao")
                .providerId("provider-1")
                .email("viewer@example.com")
                .nickname("나밍곰")
                .gender(Gender.NONE)
                .roleId(1L)
                .build();
    }
}
