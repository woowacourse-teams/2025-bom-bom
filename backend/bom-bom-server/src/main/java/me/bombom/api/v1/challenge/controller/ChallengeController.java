package me.bombom.api.v1.challenge.controller;

import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.dto.response.ChallengeEligibilityResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeInfoResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeResponse;
import me.bombom.api.v1.challenge.service.ChallengeService;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/challenges")
public class ChallengeController implements ChallengeControllerApi {

    private final ChallengeService challengeService;

    @Override
    @GetMapping
    public List<ChallengeResponse> getChallenges(@LoginMember(anonymous = true) Member member) {
        return challengeService.getChallenges(member);
    }

    @Override
    @GetMapping("/{id}")
    public ChallengeInfoResponse getChallengeInfo(@PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id) {
        return challengeService.getChallengeInfo(id);
    }

    @Override
    @GetMapping("/{id}/eligibility")
    public ChallengeEligibilityResponse checkEligibility(
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id,
            @LoginMember(anonymous = true) Member member
    ) {
        return challengeService.checkEligibility(id, member);
    }

    @Override
    @PostMapping("/{id}/application")
    @ResponseStatus(HttpStatus.CREATED)
    public void applyChallenge(
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id,
            @LoginMember Member member
    ) {
        challengeService.applyChallenge(id, member);
    }

    @Override
    @DeleteMapping("/{id}/application")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelChallenge(
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id,
            @LoginMember Member member
    ) {
        challengeService.cancelChallenge(id, member);
    }
}
