package me.bombom.api.v1.challenge.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.dto.response.ChallengeInfoResponse;
import me.bombom.api.v1.challenge.service.ChallengeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/challenges")
public class ChallengeController implements ChallengeControllerApi{

    private final ChallengeService challengeService;

    @Override
    @GetMapping("/{id}")
    public ChallengeInfoResponse getChallengeInfo(@PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id) {
        return challengeService.getChallengeInfo(id);
    }
}
