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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/maeil-mail")
public class MaeilMailController implements MaeilMailControllerApi {

    private final MaeilMailService maeilMailService;

    @Override
    @GetMapping("/{contentId}/answer")
    public MaeilMailIdealAnswerResponse getIdealAnswer(@PathVariable Long contentId) {
        return maeilMailService.getIdealAnswer(contentId);
    }

    @Override
    @PostMapping("/{contentId}/answer/me")
    public void submitAnswer(
            @LoginMember Member member,
            @PathVariable Long contentId,
            @RequestBody @Valid MaeilMailSubmitAnswerRequest request
    ) {
        maeilMailService.submitAnswer(member, contentId, request);
    }

    @Override
    @GetMapping("/{contentId}/answer/me")
    public MaeilMailSubmittedAnswerResponse getSubmittedAnswer(
            @LoginMember Member member,
            @PathVariable Long contentId
    ) {
        return maeilMailService.getSubmittedAnswer(member, contentId);
    }

    @Override
    @GetMapping("/content")
    public MaeilMailInformationResponse getInformationByArticle(@RequestParam Long articleId) {
        return maeilMailService.getContentInformationByArticle(articleId);
    }
}
