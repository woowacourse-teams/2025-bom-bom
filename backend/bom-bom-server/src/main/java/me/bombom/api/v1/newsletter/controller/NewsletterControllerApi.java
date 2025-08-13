package me.bombom.api.v1.newsletter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.rmi.ServerException;
import me.bombom.api.v1.newsletter.dto.NewsletterResponse;

import java.util.List;

@Tag(name = "Newsletter", description = "뉴스레터 관련 API")
public interface NewsletterControllerApi {

    @Operation(
        summary = "뉴스레터 목록 조회",
        description = "모든 뉴스레터 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "뉴스레터 목록 조회 성공")
    })
    List<NewsletterResponse> getNewsletters() throws ServerException;
} 
