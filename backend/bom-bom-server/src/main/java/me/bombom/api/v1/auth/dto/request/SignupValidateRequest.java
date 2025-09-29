package me.bombom.api.v1.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.auth.enums.SignupValidateField;

public record SignupValidateRequest(

        @NotNull
        SignupValidateField field,
        
        @NotBlank
        String userInput
) {
}
