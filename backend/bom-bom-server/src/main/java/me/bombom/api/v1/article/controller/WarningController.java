package me.bombom.api.v1.article.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.dto.request.UpdateWarningSettingRequest;
import me.bombom.api.v1.article.dto.response.WarningSettingResponse;
import me.bombom.api.v1.article.service.WarningService;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/articles/warning")
public class WarningController implements WarningControllerApi {

    private final WarningService warningService;

    @Override
    @GetMapping("/near-capacity")
    public WarningSettingResponse getWarningSetting(@LoginMember Member member){
        return warningService.getWarningSetting(member);
    }

    @Override
    @PostMapping("/near-capacity")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void updateWarningSetting(
            @LoginMember Member member,
            @Valid @RequestBody UpdateWarningSettingRequest request
    ){
        warningService.updateWarningSetting(member, request);
    }
}
