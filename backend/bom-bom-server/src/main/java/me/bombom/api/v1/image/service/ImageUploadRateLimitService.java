package me.bombom.api.v1.image.service;

import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.image.config.ImageUploadRateLimitProperties;
import me.bombom.api.v1.image.config.ImageUploadRateLimitProperties.Policy;
import me.bombom.api.v1.image.domain.ImageUploadBucketKey;
import me.bombom.api.v1.image.repository.ImageUploadTokenBucketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageUploadRateLimitService {

    private final ImageUploadTokenBucketRepository imageUploadTokenBucketRepository;
    private final ImageUploadRateLimitProperties properties;
    private final Clock clock;

    @Transactional
    public boolean tryConsume(ImageUploadBucketKey bucketKey, int imageCount) {
        Policy policy = properties.getPolicy(bucketKey.ownerType());
        LocalDateTime now = LocalDateTime.now(clock);

        imageUploadTokenBucketRepository.insertIfAbsent(bucketKey.value(), policy.bucketCapacity(), now);

        int affected = imageUploadTokenBucketRepository.refillAndTryConsume(
                bucketKey.value(),
                policy.bucketCapacity(),
                policy.refillSeconds(),
                imageCount,
                now
        );

        if (affected == 0) {
            log.warn("이미지 업로드 rate limit 초과 - bucketKey={}, imageCount={}", bucketKey.value(), imageCount);
            return false;
        }
        return true;
    }

    @Transactional
    public void refund(ImageUploadBucketKey bucketKey, int imageCount) {
        Policy policy = properties.getPolicy(bucketKey.ownerType());
        imageUploadTokenBucketRepository.refund(bucketKey.value(), policy.bucketCapacity(), imageCount);
    }
}
