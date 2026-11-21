package me.bombom.api.v1.inquiry.dto.response;

import java.util.List;
import java.util.Map;
import me.bombom.api.v1.inquiry.domain.InquiryMessageImage;
import me.bombom.api.v1.inquiry.dto.InquiryMessageWithAdmin;

public record InquiryMessagePageResponse(List<InquiryMessageResponse> messages, boolean hasNext) {

    public static InquiryMessagePageResponse of(
            List<InquiryMessageWithAdmin> pageMessages,
            Map<Long, List<InquiryMessageImage>> imagesByMessageId,
            boolean hasNext
    ) {
        List<InquiryMessageResponse> responses = pageMessages.stream()
                .map(row -> InquiryMessageResponse.of(
                        row.message(),
                        imagesByMessageId.getOrDefault(row.message().getId(), List.of()),
                        row.adminNickname()
                ))
                .toList();
        return new InquiryMessagePageResponse(responses, hasNext);
    }
}
