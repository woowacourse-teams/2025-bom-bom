package me.bombom.api.v1.member.controller;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.dto.response.MyChallengeSummaryResponse;
import me.bombom.api.v1.challenge.dto.response.OngoingChallengesResponse;
import me.bombom.api.v1.challenge.service.ChallengeSummaryService;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/me/challenges")
public class MyPageChallengeController implements MyPageChallengeControllerApi {

    private final ChallengeSummaryService challengeSummaryService;

    @Override
    @GetMapping("/summary")
    public MyChallengeSummaryResponse getChallengeSummary(@LoginMember Member member) {
        return challengeSummaryService.getMyChallengeSummary(member.getId());
    }

    @Override
    @GetMapping("/ongoing")
    public OngoingChallengesResponse getOngoingChallenges(@LoginMember Member member) {
        return challengeSummaryService.getOngoingChallenges(member.getId());
    }
}
