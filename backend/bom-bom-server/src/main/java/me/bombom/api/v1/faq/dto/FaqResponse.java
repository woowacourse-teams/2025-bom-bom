package me.bombom.api.v1.faq.dto;

import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.faq.domain.Faq;

public record FaqResponse(

        @NotNull
        Long faqId,

        @NotNull
        String categoryName,

        @NotNull
        String question,

        @NotNull
        String answer
) {

    public static FaqResponse from(Faq faq) {
        return new FaqResponse(
                faq.getId(),
                faq.getFaqCategory().getValue(),
                faq.getQuestion(),
                faq.getAnswer()
        );
    }
}
