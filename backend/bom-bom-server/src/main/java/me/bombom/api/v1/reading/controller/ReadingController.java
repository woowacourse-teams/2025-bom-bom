package me.bombom.api.v1.reading.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.reading.dto.request.UpdateWeeklyGoalCountRequest;
import me.bombom.api.v1.reading.dto.response.MonthlyReadingRankResponse;
import me.bombom.api.v1.reading.dto.response.ReadingInformationResponse;
import me.bombom.api.v1.reading.dto.response.WeeklyGoalCountResponse;
import me.bombom.api.v1.reading.service.ReadingService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/me/reading")
public class ReadingController implements ReadingControllerApi{

    private final ReadingService readingService;

    @Override
    @PatchMapping("/progress/week/goal")
    public WeeklyGoalCountResponse updateWeeklyGoalCount(@Valid @RequestBody UpdateWeeklyGoalCountRequest request){
        return readingService.updateWeeklyGoalCount(request);
    }

    @Override
    @GetMapping
    public ReadingInformationResponse getReadingInformation(@LoginMember Member member){
        return readingService.getReadingInformation(member);
    }

    @Override
    @GetMapping("/month/rank")
    public List<MonthlyReadingRankResponse> getMonthlyReadingRank(@RequestParam @Positive(message = "limit는 1 이상의 값이어야 합니다.") int limit) {
        return readingService.getMonthlyReadingRank(limit);
    }
}
