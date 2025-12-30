package me.bombom.api.v1.challenge.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.dto.response.MemberChallengeProgressResponse;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/challenges")
public class ChallengeProgressController implements ChallengeProgressControllerApi {

    private final ChallengeProgressService challengeProgressService;

    @Override
    @GetMapping("/{id}/progress/me")
    public MemberChallengeProgressResponse getMemberProgress(
            @LoginMember Member member,
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id
    ) {
        return challengeProgressService.getMemberProgress(id, member);
    }
}
