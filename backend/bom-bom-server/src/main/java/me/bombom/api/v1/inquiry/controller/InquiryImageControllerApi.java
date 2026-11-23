package me.bombom.api.v1.inquiry.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import me.bombom.api.v1.inquiry.dto.response.InquiryImageUploadResponse;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Inquiry", description = "1:1 문의 관련 API")
public interface InquiryImageControllerApi {

    @Operation(
            summary = "문의 이미지 업로드",
            description = "메시지에 첨부할 이미지를 S3에 업로드합니다. 최대 4장까지 한 번에 업로드할 수 있습니다. "
                    + "문의방이 생기기 전(첫 메시지 작성 시)에도 업로드할 수 있도록 문의방 소유 검증은 하지 않으며, "
                    + "무분별한 업로드는 회원/비회원 식별 검증과 요청자별 업로드 횟수 제한으로 막습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이미지 업로드 성공"),
            @ApiResponse(responseCode = "400", description = "이미지 개수가 4장을 초과하거나, 이미지 형식이 아니거나, 요청자를 식별할 수 없음"),
            @ApiResponse(responseCode = "429", description = "이미지 업로드 횟수 제한 초과")
    })
    InquiryImageUploadResponse uploadImages(
            Member member,
            String guestId,
            List<MultipartFile> images
    );
}
