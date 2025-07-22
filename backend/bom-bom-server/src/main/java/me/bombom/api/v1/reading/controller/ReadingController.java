package me.bombom.api.v1.reading.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.member.dto.request.UpdateWeeklyGoalCountRequest;
import me.bombom.api.v1.member.dto.response.ReadingInformationResponse;
import me.bombom.api.v1.member.dto.response.WeeklyGoalCountResponse;
import me.bombom.api.v1.reading.service.ReadingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/me/reading")
public class ReadingController {

    private final ReadingService readingService;

    @PatchMapping("/progress/week/goal")
    public WeeklyGoalCountResponse updateWeeklyGoalCount(@Valid @RequestBody UpdateWeeklyGoalCountRequest request){
        return readingService.updateWeeklyGoalCount(request);
    }

    @GetMapping
    public ReadingInformationResponse getReadingInformation(@RequestParam Long memberId){
        return readingService.getReadingInformation(memberId);
    }
}
