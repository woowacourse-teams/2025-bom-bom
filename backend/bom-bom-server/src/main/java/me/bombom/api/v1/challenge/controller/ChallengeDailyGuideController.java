package me.bombom.api.v1.challenge.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.dto.request.DailyGuideCommentRequest;
import me.bombom.api.v1.challenge.dto.response.CreateCommentResponse;
import me.bombom.api.v1.challenge.dto.response.DailyGuideCommentResponse;
import me.bombom.api.v1.challenge.dto.response.MemberDailyCommentResponse;
import me.bombom.api.v1.challenge.dto.response.TodayDailyGuideResponse;
import me.bombom.api.v1.challenge.service.ChallengeDailyGuideService;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping("/api/v1/challenges")
public class ChallengeDailyGuideController implements ChallengeDailyGuideControllerApi {

    private final ChallengeDailyGuideService challengeDailyGuideService;

    @Override
    @GetMapping("/{challengeId}/daily-guides/today")
    public TodayDailyGuideResponse getTodayDailyGuide(
            @LoginMember Member member,
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long challengeId
    ) {
        return challengeDailyGuideService.getTodayDailyGuide(challengeId, member.getId());
    }

    @Override
    @GetMapping("/{challengeId}/daily-guides/{dayIndex}/my-comment")
    public MemberDailyCommentResponse getDailyGuideComment(
            @LoginMember Member member,
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long challengeId,
            @PathVariable @Positive(message = "일차 인덱스는 1 이상의 값이어야 합니다.") int dayIndex
    ) {
        return challengeDailyGuideService.getDailyGuideComment(challengeId, dayIndex, member.getId());
    }

    @Override
    @PostMapping("/{challengeId}/daily-guides/{dayIndex}/my-comment")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateCommentResponse createDailyGuideComment(
            @LoginMember Member member,
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long challengeId,
            @PathVariable @Positive(message = "일차 인덱스는 1 이상의 값이어야 합니다.") int dayIndex,
            @Valid @RequestBody DailyGuideCommentRequest request
    ) {
        return challengeDailyGuideService.createDailyGuideComment(challengeId, dayIndex, member.getId(), request);
    }

    @Override
    @GetMapping("/{challengeId}/daily-guides/{dayIndex}/comments")
    public Page<DailyGuideCommentResponse> getDailyGuideComments(
            @LoginMember Member member,
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long challengeId,
            @PathVariable @Positive(message = "index는 1 이상의 값이어야 합니다.") int dayIndex,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return challengeDailyGuideService.getTotalComments(challengeId, dayIndex, member.getId(), pageable);
    }
}
