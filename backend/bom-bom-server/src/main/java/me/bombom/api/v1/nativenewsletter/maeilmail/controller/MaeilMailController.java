package me.bombom.api.v1.nativenewsletter.maeilmail.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailIdealAnswerResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailInformationResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailSubmitAnswerRequest;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailSubmittedAnswerResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.service.MaeilMailService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/maeil-mail")
public class MaeilMailController implements MaeilMailControllerApi {

    private final MaeilMailService maeilMailService;

    @Override
    @GetMapping("/{contentId}/answer")
    public MaeilMailIdealAnswerResponse getIdealAnswer(
            @LoginMember Member member,
            @PathVariable Long contentId
    ) {
        return maeilMailService.getIdealAnswer(contentId);
    }

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/articles/{articleId}/answer/me")
    public void submitAnswer(
            @LoginMember Member member,
            @PathVariable Long articleId,
            @RequestBody @Valid MaeilMailSubmitAnswerRequest request
    ) {
        maeilMailService.submitAnswer(member, articleId, request);
    }

    @Override
    @GetMapping("/articles/{articleId}/answer/me")
    public MaeilMailSubmittedAnswerResponse getSubmittedAnswer(
            @LoginMember Member member,
            @PathVariable Long articleId
    ) {
        return maeilMailService.getSubmittedAnswer(member, articleId);
    }

    @Override
    @GetMapping("/content")
    public MaeilMailInformationResponse getInformationByArticle(
            @LoginMember Member member,
            @RequestParam Long articleId
    ) {
        return maeilMailService.getContentInformationByArticle(articleId);
    }
}
