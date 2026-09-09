package me.bombom.api.v1.inquiry.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.inquiry.dto.InquiryImageUploadResponse;
import me.bombom.api.v1.inquiry.service.InquiryImageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inquiries/images")
public class InquiryImageController implements InquiryImageControllerApi {

    private final InquiryImageService inquiryImageService;

    @Override
    @PostMapping
    public InquiryImageUploadResponse uploadImages(@RequestParam("images") List<MultipartFile> images) {
        return new InquiryImageUploadResponse(inquiryImageService.uploadImages(images));
    }
}
