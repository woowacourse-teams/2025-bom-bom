package me.bombom.api.v1.newsletter.dto;

import com.querydsl.core.annotations.QueryProjection;
import jakarta.validation.constraints.NotNull;

public record CategoryResponse(

        @NotNull
        Long id,

        @NotNull
        String name
) {

    @QueryProjection
    public CategoryResponse {
    }
}
