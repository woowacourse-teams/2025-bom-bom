package me.bombom.api.v1.article.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import me.bombom.api.v1.article.dto.request.PreviousArticleRequest;
import me.bombom.api.v1.article.dto.response.PreviousArticleResponse;
import org.springframework.web.bind.annotation.ModelAttribute;

@Tag(name = "Article", description = "지난 아티클 관련 API")
public interface PreviousArticleControllerApi {

    @Operation(
            summary = "지난 아티클 목록 조회",
            description = "조건에 맞는 지난 아티클 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "지난 아티클 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 쿼리 파라미터로 요청", content = @Content)
    })
    public List<PreviousArticleResponse> getPreviousArticles(
            @Parameter(description = "필터링 관련 요청") @Valid @ModelAttribute PreviousArticleRequest previousArticleRequest
    );
}
