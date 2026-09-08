package me.bombom.api.v1.inquiry.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.bombom.api.v1.inquiry.dto.CreateInquiryRoomRequest;
import me.bombom.api.v1.inquiry.dto.InquiryRoomResponse;
import me.bombom.api.v1.member.domain.Member;

@Tag(name = "Inquiry", description = "1:1 문의 관련 API")
public interface InquiryRoomControllerApi {

    @Operation(
            summary = "문의 채팅방 생성",
            description = "카테고리를 지정해 새 문의 채팅방을 생성합니다. 비회원은 X-Guest-Id 헤더가 필요합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "채팅방 생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값이 유효하지 않음 (회원/비회원 식별 불가 등)")
    })
    InquiryRoomResponse createRoom(Member member, String guestId, @Valid CreateInquiryRoomRequest request);
}
