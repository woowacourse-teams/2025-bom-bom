package me.bombom.api.v1.newsletterrequest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateNewsletterRequestRequest(

        @NotBlank
        @Size(max = 50)
        String name,

        @NotBlank
        @Size(max = 512)
        String url,

        @Size(max = 200)
        String reason,

        boolean isNotificationEnabled
) {
}
