package me.bombom.api.v1.article.dto.response;

import me.bombom.api.v1.article.domain.WarningSetting;

public record WarningSettingResponse(
        boolean isVisible
) {

    public static WarningSettingResponse from(WarningSetting warningSetting){
        return new WarningSettingResponse(warningSetting.isVisible());
    }
}
