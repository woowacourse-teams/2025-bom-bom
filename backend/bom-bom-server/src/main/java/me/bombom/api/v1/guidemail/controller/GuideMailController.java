package me.bombom.api.v1.guidemail.controller;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.guidemail.service.GuideMailService;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/guide")
public class GuideMailController implements GuideMailControllerApi{

    private final GuideMailService guideMailService;

    @PatchMapping("/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateRead(@LoginMember Member member) {
        guideMailService.updateReadScore(member.getId());
    }
}
