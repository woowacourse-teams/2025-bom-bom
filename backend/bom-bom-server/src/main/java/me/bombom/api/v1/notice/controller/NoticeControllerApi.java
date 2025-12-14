package me.bombom.api.v1.notice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import me.bombom.api.v1.notice.dto.NoticeResponse;

@Tag(name = "Notice", description = "공지 관련 API")
public interface NoticeControllerApi {

    @Operation(
            summary = "공지 목록 조회",
            description = "공지 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "공지 목록 조회 성공")
    })
    List<NoticeResponse> getNotices();
}
