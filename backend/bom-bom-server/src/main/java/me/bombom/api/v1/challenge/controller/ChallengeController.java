package me.bombom.api.v1.challenge.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.dto.response.ChallengeResponse;
import me.bombom.api.v1.challenge.service.ChallengeService;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/challenges")
public class ChallengeController implements ChallengeControllerApi {

    private final ChallengeService challengeService;

    @Override
    public List<ChallengeResponse> getChallenges(@LoginMember(anonymous = true) Member member) {
        return challengeService.getChallenges(member);
    }
}
