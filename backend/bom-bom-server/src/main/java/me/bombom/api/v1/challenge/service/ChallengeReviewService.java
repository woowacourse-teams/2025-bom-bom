package me.bombom.api.v1.challenge.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.dto.response.ChallengeReviewListItem;
import me.bombom.api.v1.challenge.dto.response.ChallengeReviewResponse;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeReviewRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
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

    public Page<ChallengeReviewResponse> getReviews(Long challengeId, Long viewerMemberId, Pageable pageable) {
        verifyChallengeExists(challengeId);

        Pageable enforcedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                REVIEW_LIST_SORT
        );

        return challengeReviewRepository
                .findVisibleReviews(challengeId, viewerMemberId, enforcedPageable)
                .map(item -> toResponse(item, viewerMemberId));
    }

    private ChallengeReviewResponse toResponse(ChallengeReviewListItem item, Long viewerMemberId) {
        String nickname = item.nickname() != null
                ? item.nickname()
                : ChallengeReviewResponse.WITHDRAWN_MEMBER_NICKNAME;
        return new ChallengeReviewResponse(
                item.reviewId(),
                nickname,
                item.comment(),
                item.isPrivate(),
                item.memberId().equals(viewerMemberId)
        );
    }

    private void verifyChallengeExists(Long challengeId) {
        if (!challengeRepository.existsById(challengeId)) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "challenge")
                    .addContext(ErrorContextKeys.OPERATION, "getReviews");
        }
    }
}
