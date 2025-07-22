package me.bombom.api.v1.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.exception.UnauthorizedException;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.dto.request.UpdateWeeklyCurrentCountRequest;
import me.bombom.api.v1.member.dto.request.UpdateWeeklyGoalCountRequest;
import me.bombom.api.v1.member.dto.response.MemberProfileResponse;
import me.bombom.api.v1.member.dto.response.ReadingInformationResponse;
import me.bombom.api.v1.member.dto.response.WeeklyCurrentCountResponse;
import me.bombom.api.v1.member.dto.response.WeeklyGoalCountResponse;
import me.bombom.api.v1.member.service.MemberService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    public MemberProfileResponse getMember(@LoginMember Member member) {
        return memberService.getProfile(member.getId());
    }

    @PatchMapping("/me/reading/progress/week/goal")
    public WeeklyGoalCountResponse updateWeeklyGoalCount(@Valid @RequestBody UpdateWeeklyGoalCountRequest request){
        return memberService.updateWeeklyGoalCount(request);
    }

    @PatchMapping("/me/reading/progress/week/count")
    public WeeklyCurrentCountResponse updateWeeklyCurrentCount(@Valid @RequestBody UpdateWeeklyCurrentCountRequest request){
        return memberService.updateWeeklyCurrentCount(request);
    }

    @GetMapping("/me/reading")
    public ReadingInformationResponse getReadingInformation(@RequestParam Long memberId){
        return memberService.getReadingInformation(memberId);
    }
}
