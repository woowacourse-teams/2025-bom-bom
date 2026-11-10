package me.bombom.api.v1.image.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.image.domain.ImageUploadBucketKey;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageUploadTokenConsumeHandler {

    private final ImageUploadRateLimitService imageUploadRateLimitService;

    /**
     * rate limit 저장소 장애로 차감하지 못하면 업로드를 허용한다(fail-open).
     */
    public boolean consumeIfWithinRateLimit(ImageUploadBucketKey bucketKey, int imageCount) {
        try {
            return imageUploadRateLimitService.tryConsume(bucketKey, imageCount);
        } catch (DataAccessException e) {
            log.error("이미지 업로드 토큰 차감 실패 - 업로드를 허용합니다. bucketKey={}, imageCount={}",
                    bucketKey.value(), imageCount, e);
            return true;
        }
    }

    public void refund(ImageUploadBucketKey bucketKey, int imageCount) {
        try {
            imageUploadRateLimitService.refund(bucketKey, imageCount);
        } catch (DataAccessException e) {
            log.error("이미지 업로드 토큰 보상 실패. bucketKey={}, imageCount={}",
                    bucketKey.value(), imageCount, e);
        }
    }
}
