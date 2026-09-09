package me.bombom.api.v1.inquiry.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.S3ImageUploader;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.CServerErrorException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.inquiry.dto.InquiryRequester;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class InquiryImageService {

    private static final String IMAGE_PREFIX = "inquiry";
    private static final int MAX_IMAGE_COUNT = 4;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/png", "image/jpeg", "image/gif", "image/webp");

    private final S3ImageUploader imageUploader;

    @Value("${spring.cloud.aws.s3.inquiry-bucket}")
    private String inquiryBucketName;

    public List<String> uploadImages(InquiryRequester requester, List<MultipartFile> images) {
        validateRequester(requester);
        validateImageCount(images);
        images.forEach(this::validateImageContentType);

        return images.stream()
                .map(this::uploadImage)
                .toList();
    }

    private void validateRequester(InquiryRequester requester) {
        if (requester.memberId() == null && requester.guestId() == null) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                    .addContext("reason", "member_id_or_guest_id_required");
        }
    }

    private void validateImageCount(List<MultipartFile> images) {
        if (images.size() > MAX_IMAGE_COUNT) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                    .addContext("reason", "max_image_count_exceeded")
                    .addContext("imageCount", images.size());
        }
    }

    private void validateImageContentType(MultipartFile image) {
        String contentType = image.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                    .addContext("reason", "unsupported_image_content_type")
                    .addContext("contentType", contentType);
        }
    }

    private String uploadImage(MultipartFile image) {
        String objectKey = createObjectKey(image);
        try (InputStream inputStream = image.getInputStream()) {
            return imageUploader.upload(inquiryBucketName, objectKey, inputStream);
        } catch (IOException e) {
            throw new CServerErrorException(ErrorDetail.INTERNAL_SERVER_ERROR)
                    .addContext("operation", "inquiryImageStreamRead");
        }
    }

    private String createObjectKey(MultipartFile image) {
        String ext = StringUtils.getFilenameExtension(image.getOriginalFilename());
        String fileName = StringUtils.hasText(ext) ? UUID.randomUUID() + "." + ext : UUID.randomUUID().toString();
        return IMAGE_PREFIX + "/" + fileName;
    }
}
