package me.bombom.api.v1.faq.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.bombom.api.v1.faq.dto.FaqResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;

@Tag(name = "Faq", description = "FAQ 관련 API")
public interface FaqControllerApi {

    @Operation(
            summary = "FAQ 목록 조회",
            description = "FAQ 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "FAQ 목록 조회 성공")
    })
    Page<FaqResponse> getFaqs(
            @PageableDefault(size = 20)
            @SortDefault.SortDefaults({
                    @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC),
                    @SortDefault(sort = "id", direction = Sort.Direction.ASC)
            }) Pageable pageable
    );
}
