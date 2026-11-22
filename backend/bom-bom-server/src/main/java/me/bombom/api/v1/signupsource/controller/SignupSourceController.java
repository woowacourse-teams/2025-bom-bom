package me.bombom.api.v1.signupsource.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.signupsource.dto.request.SaveSignupSourceRequest;
import me.bombom.api.v1.signupsource.dto.response.SignupSourceStatusResponse;
import me.bombom.api.v1.signupsource.service.SignupSourceService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/me/signup-source")
public class SignupSourceController implements SignupSourceControllerApi {

    private final SignupSourceService signupSourceService;

    @Override
    @GetMapping
    public SignupSourceStatusResponse getSignupSourceStatus(@LoginMember Member member) {
        return signupSourceService.getStatus(member);
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void saveSignupSource(
            @LoginMember Member member,
            @Valid @RequestBody SaveSignupSourceRequest request
    ) {
        signupSourceService.save(member, request);
    }
}
