package me.bombom.api.v1.challenge.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.dto.MemberChallengeProgressResponse;
import me.bombom.api.v1.challenge.service.ChallengeProgressService;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/challenges")
public class ChallengeProgressController {

    private final ChallengeProgressService challengeProgressService;

    @GetMapping("/{id}/progress/me")
    public MemberChallengeProgressResponse getMemberProgress(
            @LoginMember Member member,
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id
    ) {
        return challengeProgressService.getMemberProgress(id, member);
    }
}
