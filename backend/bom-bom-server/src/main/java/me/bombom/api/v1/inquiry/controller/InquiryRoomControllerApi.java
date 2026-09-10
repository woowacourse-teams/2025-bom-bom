package me.bombom.api.v1.inquiry.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.bombom.api.v1.inquiry.dto.request.CreateInquiryRoomRequest;
import me.bombom.api.v1.inquiry.dto.response.InquiryRoomResponse;
import me.bombom.api.v1.member.domain.Member;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    @Operation(
            summary = "문의 채팅방 목록 조회",
            description = "생성일 최신순으로 문의 채팅방 목록을 조회합니다. 종료된 채팅방도 포함됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "채팅방 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값이 유효하지 않음 (회원/비회원 식별 불가 등)")
    })
    Page<InquiryRoomResponse> getRooms(Member member, String guestId, @ParameterObject Pageable pageable);
}
