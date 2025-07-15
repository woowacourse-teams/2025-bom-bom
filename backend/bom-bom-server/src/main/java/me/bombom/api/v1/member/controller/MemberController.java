package me.bombom.api.v1.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.member.dto.request.UpdateWeeklyGoalRequest;
import me.bombom.api.v1.member.dto.response.WeeklyGoalResponse;
import me.bombom.api.v1.member.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;

    @PatchMapping("/me/reading/progress/week/goal")
    public WeeklyGoalResponse updateWeeklyGoal(@Valid @RequestBody UpdateWeeklyGoalRequest request){
        return memberService.updateWeeklyGoal(request);
    }
}
