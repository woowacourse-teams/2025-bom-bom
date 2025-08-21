package me.bombom.api.v1.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.auth.enums.DuplicateCheckField;

public record DuplicateCheckRequest(
        @NotNull
        DuplicateCheckField field,
        
        @NotBlank
        String value
) {
}
