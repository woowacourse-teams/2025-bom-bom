package me.bombom.api.v1.inquiry.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.bombom.api.v1.inquiry.dto.InquiryMessagePageResponse;
import me.bombom.api.v1.inquiry.dto.InquiryMessageResponse;
import me.bombom.api.v1.inquiry.dto.SendInquiryMessageRequest;
import me.bombom.api.v1.inquiry.dto.UpdateInquiryMessageRequest;
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

    @Operation(
            summary = "문의 메시지 조회",
            description = "채팅방의 메시지를 커서 기반으로 조회합니다. cursor가 없으면 최신 메시지부터 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메시지 조회 성공"),
            @ApiResponse(responseCode = "403", description = "본인 소유 채팅방이 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 채팅방")
    })
    InquiryMessagePageResponse getMessages(
            Member member,
            String guestId,
            @Parameter(description = "채팅방 ID") Long roomId,
            @Parameter(description = "커서 (마지막으로 받은 메시지 ID)") Long cursor,
            @Parameter(description = "조회 개수") int size
    );

    @Operation(
            summary = "문의 메시지 수정",
            description = "본인이 작성한 메시지의 내용을 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메시지 수정 성공"),
            @ApiResponse(responseCode = "403", description = "본인이 작성한 메시지가 아니거나 본인 소유 채팅방이 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 채팅방 또는 메시지")
    })
    InquiryMessageResponse updateMessage(
            Member member,
            String guestId,
            @Parameter(description = "채팅방 ID") Long roomId,
            @Parameter(description = "메시지 ID") Long messageId,
            @Valid UpdateInquiryMessageRequest request
    );

    @Operation(
            summary = "문의 메시지 삭제",
            description = "본인이 작성한 메시지를 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "메시지 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "본인이 작성한 메시지가 아니거나 본인 소유 채팅방이 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 채팅방 또는 메시지")
    })
    void deleteMessage(
            Member member,
            String guestId,
            @Parameter(description = "채팅방 ID") Long roomId,
            @Parameter(description = "메시지 ID") Long messageId
    );
}
