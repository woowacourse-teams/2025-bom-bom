package me.bombom.api.v1.challenge.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.controller.mock.ChallengeReviewMockStore;
import me.bombom.api.v1.challenge.controller.mock.ChallengeReviewMockStore.MockReview;
import me.bombom.api.v1.challenge.dto.request.CreateChallengeReviewRequest;
import me.bombom.api.v1.challenge.dto.request.UpdateChallengeReviewRequest;
import me.bombom.api.v1.challenge.dto.response.ChallengeReviewResponse;
import me.bombom.api.v1.challenge.dto.response.MyChallengeReviewResponse;
import me.bombom.api.v1.challenge.service.ChallengeReviewService;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/challenges/{challengeId}/reviews")
public class ChallengeReviewController implements ChallengeReviewControllerApi {

    // TODO: 구현 전체 전환 시 Mock 저장소 제거
    private final ChallengeReviewMockStore mockStore;
    private final ChallengeReviewService challengeReviewService;

    @Override
    @GetMapping
    public Page<ChallengeReviewResponse> getReviews(
            @PathVariable @Positive(message = "challengeId는 1 이상의 값이어야 합니다.") Long challengeId,
            @LoginMember Member member,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return challengeReviewService.getReviews(challengeId, member.getId(), pageable);
    }

    // TODO: Service 도입 시 challengeId + memberId 기준 조회로 보강
    @Override
    @GetMapping("/me")
    public MyChallengeReviewResponse getMyReview(
            @PathVariable @Positive(message = "challengeId는 1 이상의 값이어야 합니다.") Long challengeId,
            @LoginMember Member member
    ) {
        MockReview mine = mockStore.findByNickname(member.getNickname())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeReview")
                        .addContext(ErrorContextKeys.OPERATION, "getMyReview"));
        return ChallengeReviewMockStore.toMyResponse(mine);
    }

    // TODO: Service 도입 시 challengeId + memberId 기준 중복 검사로 보강
    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createReview(
            @PathVariable @Positive(message = "challengeId는 1 이상의 값이어야 합니다.") Long challengeId,
            @Valid @RequestBody CreateChallengeReviewRequest request,
            @LoginMember Member member
    ) {
        if (mockStore.findByNickname(member.getNickname()).isPresent()) {
            throw new CIllegalArgumentException(ErrorDetail.DUPLICATED_DATA)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeReview")
                    .addContext(ErrorContextKeys.OPERATION, "createReview");
        }
        mockStore.save(member.getNickname(), request.comment(), request.isPrivate());
    }

    @Override
    @PutMapping("/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateReview(
            @PathVariable @Positive(message = "challengeId는 1 이상의 값이어야 합니다.") Long challengeId,
            @PathVariable @Positive(message = "reviewId는 1 이상의 값이어야 합니다.") Long reviewId,
            @Valid @RequestBody UpdateChallengeReviewRequest request,
            @LoginMember Member member
    ) {
        MockReview existing = mockStore.findById(reviewId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeReview")
                        .addContext(ErrorContextKeys.OPERATION, "updateReview"));

        if (!existing.nickname().equals(member.getNickname())) {
            throw new CIllegalArgumentException(ErrorDetail.FORBIDDEN_RESOURCE)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeReview")
                    .addContext(ErrorContextKeys.OPERATION, "updateReview");
        }

        mockStore.updateById(reviewId, request.comment(), request.isPrivate());
    }
}
