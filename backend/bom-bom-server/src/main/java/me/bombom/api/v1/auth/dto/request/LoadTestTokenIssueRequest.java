package me.bombom.api.v1.auth.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.Collections;
import java.util.List;
import lombok.NonNull;

public record LoadTestTokenIssueRequest(
    @NotEmpty List<@NonNull @Min(1) Long> memberIds
) {

    public LoadTestTokenIssueRequest {
        if (memberIds == null) {
            memberIds = Collections.emptyList();
        }
    }
}

