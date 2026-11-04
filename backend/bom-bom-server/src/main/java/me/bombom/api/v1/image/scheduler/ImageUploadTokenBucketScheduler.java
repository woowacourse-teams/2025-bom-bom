package me.bombom.api.v1.image.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.image.service.ImageUploadRateLimitService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageUploadTokenBucketScheduler {

    private static final String TIME_ZONE = "Asia/Seoul";
    private static final String GUEST_BUCKET_CLEANUP_CRON = "0 0 4 * * *";

    private final ImageUploadRateLimitService imageUploadRateLimitService;

    @Scheduled(cron = GUEST_BUCKET_CLEANUP_CRON, zone = TIME_ZONE)
    @SchedulerLock(name = "cleanup_expired_guest_image_upload_buckets", lockAtLeastFor = "PT4S", lockAtMostFor = "PT5M")
    public void cleanupExpiredGuestBuckets() {
        log.info("비회원 이미지 업로드 버킷 정리 시작");
        try {
            imageUploadRateLimitService.cleanupExpiredGuestBuckets();
            log.info("비회원 이미지 업로드 버킷 정리 종료");
        } catch (Exception e) {
            log.warn("비회원 이미지 업로드 버킷 정리 실패", e);
        }
    }
}
