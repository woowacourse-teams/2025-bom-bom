package me.bombom.api.v1.inquiry.controller;

import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.inquiry.dto.InquiryRequester;
import me.bombom.api.v1.inquiry.dto.response.InquiryImageUploadResponse;
import me.bombom.api.v1.inquiry.resolver.GuestId;
import me.bombom.api.v1.inquiry.service.InquiryImageService;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inquiries/images")
public class InquiryImageController implements InquiryImageControllerApi {

    private final InquiryImageService inquiryImageService;

    @Override
    @PostMapping
    public InquiryImageUploadResponse uploadImages(
            @LoginMember(anonymous = true) Member member,
            @GuestId String guestId,
            @RequestParam @Positive(message = "id는 1 이상의 값이어야 합니다.") Long roomId,
            @RequestParam("images") List<MultipartFile> images
    ) {
        InquiryRequester requester = InquiryRequester.of(member, guestId);
        return InquiryImageUploadResponse.of(inquiryImageService.uploadImages(requester, roomId, images));
    }
}
