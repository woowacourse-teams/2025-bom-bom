package me.bombom.api.v1.challenge.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.dto.response.ChallengeReviewResponse;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/challenges/reviews")
public class ChallengeReviewController implements ChallengeReviewControllerApi {

    @Override
    @GetMapping
    public List<ChallengeReviewResponse> getReviews(
            @LoginMember Member member
    ) {
        // TODO: 실제 조회 로직 구현
        return List.of(
                new ChallengeReviewResponse(1L, "나밍곰", "내가 쓴 비공개 리뷰입니다.", true),
                new ChallengeReviewResponse(2L, "제나", "정말 유익한 챌린지였어요!", false),
                new ChallengeReviewResponse(3L, "밍곰", "다음에도 또 참여하고 싶어요.", false)
        );
    }
}
