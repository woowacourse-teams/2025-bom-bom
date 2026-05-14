package me.bombom.api.v1.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import me.bombom.api.v1.member.domain.WarningSetting;

public record WarningSettingResponse(

        @Schema(required = true)
        boolean isVisible
) {

    public static WarningSettingResponse from(WarningSetting warningSetting){
        return new WarningSettingResponse(warningSetting.isVisible());
    }
}
