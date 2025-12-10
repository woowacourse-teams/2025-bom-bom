package me.bombom.api.v1.article.controller;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.dto.response.WarningSettingResponse;
import me.bombom.api.v1.article.service.WarningService;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/articles/warning")
public class WarningController implements WarningControllerApi {

    private final WarningService warningService;

    @Override
    @GetMapping("/near-capacity")
    public WarningSettingResponse getCapacityWarningStatus(@LoginMember Member member){
        return warningService.getCapacityWarningStatus(member);
    }
}
