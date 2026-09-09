package me.bombom.api.v1.inquiry.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import me.bombom.api.v1.inquiry.dto.InquiryImageUploadResponse;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Inquiry", description = "1:1 문의 관련 API")
public interface InquiryImageControllerApi {

    @Operation(
            summary = "문의 이미지 업로드",
            description = "메시지에 첨부할 이미지를 S3에 업로드합니다. 최대 4장까지 한 번에 업로드할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이미지 업로드 성공"),
            @ApiResponse(responseCode = "400", description = "이미지 개수가 4장을 초과함")
    })
    InquiryImageUploadResponse uploadImages(List<MultipartFile> images);
}
