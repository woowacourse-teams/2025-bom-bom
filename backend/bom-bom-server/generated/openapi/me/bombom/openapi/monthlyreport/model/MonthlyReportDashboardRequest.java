package me.bombom.openapi.monthlyreport.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import jakarta.annotation.Generated;

/**
 * 월간 리포트 대시보드 조회 조건
 */
@Generated("org.openapitools.codegen.languages.SpringCodegen")
public record MonthlyReportDashboardRequest(

        @Min(1)
        @Schema(description = "조회할 연도", requiredMode = REQUIRED)
        int year,

        @Min(1)
        @Max(12)
        @Schema(description = "조회할 월 (1-12)", requiredMode = REQUIRED)
        int month,

        @Min(1)
        @Schema(description = "자주 읽은 뉴스레터 조회 개수", requiredMode = REQUIRED)
        int limit
) {
}
