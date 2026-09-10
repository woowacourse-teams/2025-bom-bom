package me.bombom.api.v1.inquiry.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import me.bombom.api.v1.inquiry.dto.response.InquiryCategoryResponse;

@Tag(name = "Inquiry", description = "1:1 문의 관련 API")
public interface InquiryCategoryControllerApi {

    @Operation(
            summary = "문의 카테고리 목록 조회",
            description = "문의 채팅방 생성 시 선택 가능한 카테고리 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "카테고리 목록 조회 성공")
    })
    List<InquiryCategoryResponse> getCategories();
}
