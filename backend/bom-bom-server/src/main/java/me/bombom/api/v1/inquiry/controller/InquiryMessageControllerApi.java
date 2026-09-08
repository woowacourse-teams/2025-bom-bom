package me.bombom.api.v1.inquiry.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.bombom.api.v1.inquiry.dto.InquiryMessageResponse;
import me.bombom.api.v1.inquiry.dto.SendInquiryMessageRequest;
import me.bombom.api.v1.member.domain.Member;

@Tag(name = "Inquiry", description = "1:1 문의 관련 API")
public interface InquiryMessageControllerApi {

    @Operation(
            summary = "문의 메시지 전송",
            description = "채팅방에 메시지를 전송합니다. 이미지는 최대 4장까지 첨부할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메시지 전송 성공"),
            @ApiResponse(responseCode = "403", description = "본인 소유 채팅방이 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 채팅방")
    })
    InquiryMessageResponse sendMessage(
            Member member,
            String guestId,
            @Parameter(description = "채팅방 ID") Long roomId,
            @Valid SendInquiryMessageRequest request
    );
}
