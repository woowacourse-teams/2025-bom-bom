package me.bombom.api.v1.inquiry.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import me.bombom.api.v1.inquiry.dto.request.CreateInquiryRoomRequest;
import me.bombom.api.v1.inquiry.dto.response.InquiryRoomResponse;
import me.bombom.api.v1.inquiry.dto.response.InquiryUnreadStatusResponse;
import me.bombom.api.v1.member.domain.Member;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Tag(name = "Inquiry", description = "1:1 문의 관련 API")
public interface InquiryRoomControllerApi {

    @Operation(
            summary = "첫 메시지와 함께 문의 채팅방 생성",
            description = "카테고리와 첫 메시지(내용/이미지)를 함께 받아 문의 채팅방을 생성하고 첫 메시지를 작성합니다. "
                    + "방 생성과 첫 메시지 작성은 하나의 트랜잭션으로 처리되어, 메시지 작성에 실패하면 방도 생성되지 않습니다. "
                    + "내용(최대 500자)과 이미지(최대 4장) 중 하나 이상은 필수입니다. "
                    + "응답의 lastMessage에 작성된 첫 메시지가 담깁니다. 비회원은 X-Guest-Id 헤더가 필요합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "채팅방 및 첫 메시지 생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값이 유효하지 않음 (회원/비회원 식별 불가, 내용·이미지 모두 없음, 길이·개수 초과 등)"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리")
    })
    InquiryRoomResponse createRoom(Member member, String guestId, @Valid CreateInquiryRoomRequest request);

    @Operation(
            summary = "문의 채팅방 목록 조회",
            description = "마지막 메시지가 최신인 순(메시지 id 내림차순)으로 문의 채팅방 목록을 조회합니다. 메시지가 없는 채팅방은 맨 뒤에 방 id 내림차순으로 정렬됩니다. 종료된 채팅방도 포함됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "채팅방 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값이 유효하지 않음 (회원/비회원 식별 불가 등)")
    })
    Page<InquiryRoomResponse> getRooms(Member member, String guestId, @ParameterObject Pageable pageable);

    @Operation(
            summary = "문의 채팅방 단건 조회",
            description = "roomId로 문의 채팅방 상세 정보를 조회합니다. 본인이 소유한 채팅방만 조회할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "채팅방 단건 조회 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값이 유효하지 않음 (회원/비회원 식별 불가 등)"),
            @ApiResponse(responseCode = "403", description = "본인이 소유한 채팅방이 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 채팅방")
    })
    InquiryRoomResponse getRoom(
            Member member,
            String guestId,
            @Parameter(description = "채팅방 ID") @Positive(message = "id는 1 이상의 값이어야 합니다.") Long roomId
    );

    @Operation(
            summary = "문의 미확인 상태 조회",
            description = "내 문의 채팅방 중 아직 읽지 않은 어드민 응답이 하나라도 있는지 여부를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "미확인 상태 조회 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값이 유효하지 않음 (회원/비회원 식별 불가 등)")
    })
    InquiryUnreadStatusResponse getUnreadStatus(Member member, String guestId);
}
