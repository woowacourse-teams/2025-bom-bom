package me.bombom.api.v1.challenge.service;

import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeReview;
import me.bombom.api.v1.challenge.dto.request.CreateChallengeReviewRequest;
import me.bombom.api.v1.challenge.dto.request.UpdateChallengeReviewRequest;
import me.bombom.api.v1.challenge.dto.response.ChallengeReviewResponse;
import me.bombom.api.v1.challenge.dto.response.MyChallengeReviewResponse;
import me.bombom.api.v1.challenge.event.CreateChallengeReviewEvent;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeReviewRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeReviewService {

    private static final Sort REVIEW_LIST_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    private final ChallengeReviewRepository challengeReviewRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeParticipantRepository challengeParticipantRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final Clock clock;

    public Page<ChallengeReviewResponse> getReviews(Long challengeId, Long viewerMemberId, Pageable pageable) {
        verifyChallengeExists(challengeId);

        Pageable enforcedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                REVIEW_LIST_SORT
        );

        return challengeReviewRepository
                .findVisibleReviews(challengeId, viewerMemberId, enforcedPageable)
                .map(item -> ChallengeReviewResponse.of(item, viewerMemberId));
    }

    public MyChallengeReviewResponse getMyReview(Long challengeId, Member viewer) {
        verifyChallengeExists(challengeId);

        ChallengeReview review = challengeReviewRepository
                .findByChallengeIdAndMemberId(challengeId, viewer.getId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeReview")
                        .addContext(ErrorContextKeys.OPERATION, "findByChallengeIdAndMemberId")
                        .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId)
                        .addContext(ErrorContextKeys.MEMBER_ID, viewer.getId()));

        return MyChallengeReviewResponse.of(review, viewer.getNickname());
    }

    @Transactional
    public void createReview(Long challengeId, Member viewer, CreateChallengeReviewRequest request) {
        Challenge challenge = getChallenge(challengeId);
        LocalDate today = LocalDate.now(clock);
        verifyReviewWritablePeriod(challenge, today);
        verifyNoDuplicateReview(challengeId, viewer.getId());

        ChallengeParticipant participant = challengeParticipantRepository
                .findByChallengeIdAndMemberId(challengeId, viewer.getId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeParticipant")
                        .addContext(ErrorContextKeys.OPERATION, "findByChallengeIdAndMemberId")
                        .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId)
                        .addContext(ErrorContextKeys.MEMBER_ID, viewer.getId()));

        ChallengeReview review = ChallengeReview.builder()
                .challengeId(challengeId)
                .memberId(viewer.getId())
                .comment(request.comment())
                .isPrivate(request.isPrivate())
                .build();

        try {
            challengeReviewRepository.save(review);
        } catch (DataIntegrityViolationException e) {
            throw new CIllegalArgumentException(ErrorDetail.DUPLICATED_DATA)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeReview")
                    .addContext(ErrorContextKeys.OPERATION, "save")
                    .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId)
                    .addContext(ErrorContextKeys.MEMBER_ID, viewer.getId());
        }

        // 챌린지 기간 내 작성한 경우에만 출석 인정 (종료 후 늦은 작성은 리뷰만 저장)
        if (challenge.isWithinPeriod(today)) {
            applicationEventPublisher.publishEvent(
                    new CreateChallengeReviewEvent(participant.getId(), today)
            );
        }
    }

    @Transactional
    public void updateReview(Long challengeId, Long reviewId, Member viewer, UpdateChallengeReviewRequest request) {
        ChallengeReview review = challengeReviewRepository.findById(reviewId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeReview")
                        .addContext(ErrorContextKeys.OPERATION, "findById")
                        .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId)
                        .addContext(ErrorContextKeys.MEMBER_ID, viewer.getId()));

        if (!review.getChallengeId().equals(challengeId)) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeReview")
                    .addContext(ErrorContextKeys.OPERATION, "verifyChallengeIdMatch")
                    .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId)
                    .addContext(ErrorContextKeys.MEMBER_ID, viewer.getId());
        }

        if (!review.isOwnedBy(viewer.getId())) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeReview")
                    .addContext(ErrorContextKeys.OPERATION, "isOwnedBy")
                    .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId)
                    .addContext(ErrorContextKeys.MEMBER_ID, viewer.getId());
        }

        review.update(request.comment(), request.isPrivate());
    }

    private void verifyNoDuplicateReview(Long challengeId, Long memberId) {
        if (challengeReviewRepository.existsByChallengeIdAndMemberId(challengeId, memberId)) {
            throw new CIllegalArgumentException(ErrorDetail.DUPLICATED_DATA)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeReview")
                    .addContext(ErrorContextKeys.OPERATION, "existsByChallengeIdAndMemberId")
                    .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId)
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId);
        }
    }

    private void verifyChallengeExists(Long challengeId) {
        if (!challengeRepository.existsById(challengeId)) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "challenge")
                    .addContext(ErrorContextKeys.OPERATION, "existsById")
                    .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId);
        }
    }

    private Challenge getChallenge(Long challengeId) {
        return challengeRepository.findById(challengeId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challenge")
                        .addContext(ErrorContextKeys.OPERATION, "findById")
                        .addContext(ErrorContextKeys.CHALLENGE_ID, challengeId));
    }

    private void verifyReviewWritablePeriod(Challenge challenge, LocalDate today) {
        if (!challenge.hasStarted(today)) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "challenge")
                    .addContext(ErrorContextKeys.OPERATION, "verifyReviewWritablePeriod")
                    .addContext(ErrorContextKeys.CHALLENGE_ID, challenge.getId())
                    .addContext("reason", "챌린지 시작일 이전에는 리뷰를 작성할 수 없습니다.");
        }
    }
}
