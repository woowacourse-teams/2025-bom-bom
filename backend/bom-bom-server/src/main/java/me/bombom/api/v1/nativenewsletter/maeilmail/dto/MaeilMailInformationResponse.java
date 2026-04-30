package me.bombom.api.v1.nativenewsletter.maeilmail.dto;

import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailIssueHistory;

public record MaeilMailInformationResponse(

        @NotNull
        Long contentId
) {

    public static MaeilMailInformationResponse from(MaeilMailIssueHistory history) {
        return new MaeilMailInformationResponse(history.getContentId());
    }
}
