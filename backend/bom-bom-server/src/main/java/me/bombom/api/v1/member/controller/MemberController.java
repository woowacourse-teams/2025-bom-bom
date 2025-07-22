package me.bombom.api.v1.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.member.dto.request.UpdateWeeklyCurrentCountRequest;
import me.bombom.api.v1.member.dto.request.UpdateWeeklyGoalCountRequest;
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

    @PatchMapping("/me/reading/progress/week/goal")
    public WeeklyGoalCountResponse updateWeeklyGoalCount(@Valid @RequestBody UpdateWeeklyGoalCountRequest request){
        return memberService.updateWeeklyGoalCount(request);
    }

    @GetMapping("/me/reading")
    public ReadingInformationResponse getReadingInformation(@RequestParam Long memberId){
        return memberService.getReadingInformation(memberId);
    }
}
