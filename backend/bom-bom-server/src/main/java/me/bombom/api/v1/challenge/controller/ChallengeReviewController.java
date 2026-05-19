package me.bombom.api.v1.challenge.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.controller.mock.ChallengeReviewMockStore;
import me.bombom.api.v1.challenge.dto.request.CreateChallengeReviewRequest;
import me.bombom.api.v1.challenge.dto.response.ChallengeReviewResponse;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/challenges/{challengeId}/reviews")
public class ChallengeReviewController implements ChallengeReviewControllerApi {

    // TODO: Service 계층 도입 시 Mock 저장소 제거
    private final ChallengeReviewMockStore mockStore;

    @Override
    @GetMapping
    public List<ChallengeReviewResponse> getReviews(
            @PathVariable @Positive(message = "challengeId는 1 이상의 값이어야 합니다.") Long challengeId,
            @LoginMember Member member
    ) {
        return mockStore.findAll();
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createReview(
            @PathVariable @Positive(message = "challengeId는 1 이상의 값이어야 합니다.") Long challengeId,
            @Valid @RequestBody CreateChallengeReviewRequest request,
            @LoginMember Member member
    ) {
        mockStore.save(member.getNickname(), request.comment(), request.isPrivate());
    }
}
