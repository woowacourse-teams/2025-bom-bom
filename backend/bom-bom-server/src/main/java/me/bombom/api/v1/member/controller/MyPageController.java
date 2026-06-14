package me.bombom.api.v1.member.controller;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.service.MemberService;
import me.bombom.openapi.mypage.api.MyPageApi;
import me.bombom.openapi.mypage.model.MemberJoinDaysResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/me")
public class MyPageController implements MyPageApi {

    private final MemberService memberService;

    @Override
    @GetMapping("/join-days")
    public MemberJoinDaysResponse getMemberJoinDays(@LoginMember Member member) {
        return memberService.getJoinDays(member);
    }
}
