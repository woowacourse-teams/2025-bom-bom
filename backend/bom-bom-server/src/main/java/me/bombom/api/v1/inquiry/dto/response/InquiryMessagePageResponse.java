package me.bombom.api.v1.inquiry.dto.response;

import java.util.List;
import java.util.Map;
import me.bombom.api.v1.inquiry.domain.InquiryMessage;
import me.bombom.api.v1.inquiry.domain.InquiryMessageImage;

public record InquiryMessagePageResponse(List<InquiryMessageResponse> messages, boolean hasNext) {

    public static InquiryMessagePageResponse of(
            List<InquiryMessage> pageMessages,
            Map<Long, List<InquiryMessageImage>> imagesByMessageId,
            boolean hasNext
    ) {
        List<InquiryMessageResponse> responses = pageMessages.stream()
                .map(message -> InquiryMessageResponse.of(
                        message,
                        imagesByMessageId.getOrDefault(message.getId(), List.of())
                ))
                .toList();
        return new InquiryMessagePageResponse(responses, hasNext);
    }
}
