package news.bombom.notification.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import news.bombom.notification.domain.DevicePlatform;
import news.bombom.notification.domain.NotificationCategory;
import news.bombom.notification.domain.NotificationStatus;
import news.bombom.notification.domain.SendResult.SendStatus;
import news.bombom.notification.dto.response.stats.CategoryStatsResponse;
import news.bombom.notification.dto.response.stats.CategoryStatsResponse.DeliveryMode;
import news.bombom.notification.dto.response.stats.CategoryStatsResponse.DeviceSendsResponse;
import news.bombom.notification.dto.response.stats.CategoryStatsResponse.FailureReasonResponse;
import news.bombom.notification.dto.response.stats.CategoryStatsResponse.OpensResponse;
import news.bombom.notification.dto.response.stats.NotificationStatsResponse;
import news.bombom.notification.dto.response.stats.PlatformOpenCountResponse;
import news.bombom.notification.dto.response.stats.ReachResponse;
import news.bombom.notification.repository.NotificationStatsQueryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * 어드민 알림 지표 집계. 날짜는 Asia/Seoul 기준이며 from·to를 모두 포함한다.
 * 발송·기기 로그는 발송 시각, 열림은 열림 시각 기준으로 기간에 귀속한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationStatsService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final long MAX_PERIOD_DAYS = 90;
    private static final String UNKNOWN_ERROR_CODE = "UNKNOWN";

    private final NotificationStatsQueryRepository statsQueryRepository;

    public NotificationStatsResponse getStats(LocalDate from, LocalDate to) {
        validatePeriod(from, to);
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        List<CategoryStatsResponse> categories = Arrays.stream(NotificationCategory.values())
                .map(category -> category.isUseTopic()
                        ? getTopicCategoryStats(category, start, end)
                        : getMemberCategoryStats(category, start, end))
                .toList();

        return new NotificationStatsResponse(
                OffsetDateTime.now(SEOUL),
                from,
                to,
                categories,
                getReach(),
                getOpensByPlatform(start, end)
        );
    }

    private void validatePeriod(LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "to는 from보다 이전일 수 없습니다.");
        }
        if (ChronoUnit.DAYS.between(from, to) + 1 > MAX_PERIOD_DAYS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "조회 기간은 최대 " + MAX_PERIOD_DAYS + "일입니다.");
        }
    }

    private CategoryStatsResponse getMemberCategoryStats(NotificationCategory category,
                                                         LocalDateTime start, LocalDateTime end) {
        Map<NotificationStatus, Long> statusCounts = getStatusCounts(category, start, end);
        Map<Long, LocalDateTime> firstOpenedAt = statsQueryRepository.findFirstOpenedAtByNotificationId(category, start, end);
        long uniqueLinked = firstOpenedAt.size();
        Map<SendStatus, Long> deviceSends = statsQueryRepository.countDeviceSendsByStatus(category, start, end);

        return new CategoryStatsResponse(
                category,
                DeliveryMode.MEMBER,
                statusCounts,
                getFailureReasons(category, start, end),
                new DeviceSendsResponse(
                        deviceSends.getOrDefault(SendStatus.SUCCESS, 0L),
                        deviceSends.getOrDefault(SendStatus.FAILED, 0L)),
                new OpensResponse(
                        statsQueryRepository.countLinkedOpens(category, start, end),
                        uniqueLinked,
                        statsQueryRepository.countUnlinkedOpens(category, start, end)),
                divide(uniqueLinked, statusCounts.get(NotificationStatus.SENT)),
                getMedianSecondsToOpen(category, firstOpenedAt),
                statsQueryRepository.countConsentingMembers(category)
        );
    }

    private CategoryStatsResponse getTopicCategoryStats(NotificationCategory category,
                                                        LocalDateTime start, LocalDateTime end) {
        long uniqueLinked = statsQueryRepository.countDistinctScheduleMemberOpens(category, start, end);
        long consentingMembers = statsQueryRepository.countConsentingMembers(category);

        return new CategoryStatsResponse(
                category,
                DeliveryMode.TOPIC,
                null,
                List.of(),
                null,
                new OpensResponse(
                        statsQueryRepository.countLinkedOpens(category, start, end),
                        uniqueLinked,
                        statsQueryRepository.countUnlinkedOpens(category, start, end)),
                divide(uniqueLinked, consentingMembers),
                null,
                consentingMembers
        );
    }

    private Map<NotificationStatus, Long> getStatusCounts(NotificationCategory category,
                                                          LocalDateTime start, LocalDateTime end) {
        Map<NotificationStatus, Long> counted = statsQueryRepository.countByStatus(category, start, end);
        Map<NotificationStatus, Long> statusCounts = new LinkedHashMap<>();
        for (NotificationStatus status : NotificationStatus.values()) {
            statusCounts.put(status, counted.getOrDefault(status, 0L));
        }
        if (category == NotificationCategory.ARTICLE) {
            statusCounts.merge(NotificationStatus.DEAD,
                    statsQueryRepository.countMovedArticleFailures(start, end), Long::sum);
        }
        return statusCounts;
    }

    /**
     * 회원 단위 사유(토큰 없음)와 기기 단위 FCM 에러 코드를 함께 센다.
     */
    private List<FailureReasonResponse> getFailureReasons(NotificationCategory category,
                                                          LocalDateTime start, LocalDateTime end) {
        List<FailureReasonResponse> reasons = new ArrayList<>();

        long noToken = statsQueryRepository.countByLastError(
                category, NotificationProcessingService.NO_TOKEN_ERROR, start, end);
        if (category == NotificationCategory.ARTICLE) {
            noToken += statsQueryRepository.countMovedArticleFailuresByLastError(
                    NotificationProcessingService.NO_TOKEN_ERROR, start, end);
        }
        if (noToken > 0) {
            reasons.add(new FailureReasonResponse(NotificationProcessingService.NO_TOKEN_ERROR, noToken));
        }

        statsQueryRepository.countDeviceFailuresByErrorCode(category, start, end)
                .forEach((errorCode, count) -> reasons.add(
                        new FailureReasonResponse(Objects.requireNonNullElse(errorCode, UNKNOWN_ERROR_CODE), count)));

        reasons.sort(Comparator.comparingLong(FailureReasonResponse::count).reversed());
        return reasons;
    }

    /**
     * 알림 row 생성 시각부터 첫 열림까지 걸린 시간의 중앙값(초). 짝수 개면 낮은 쪽 중앙값을 쓴다.
     */
    private Long getMedianSecondsToOpen(NotificationCategory category, Map<Long, LocalDateTime> firstOpenedAt) {
        if (firstOpenedAt.isEmpty()) {
            return null;
        }
        Map<Long, LocalDateTime> createdAt = statsQueryRepository.findCreatedAtByIds(category, firstOpenedAt.keySet());
        List<Long> seconds = firstOpenedAt.entrySet().stream()
                .filter(entry -> createdAt.containsKey(entry.getKey()))
                .map(entry -> Math.max(0, Duration.between(createdAt.get(entry.getKey()), entry.getValue()).getSeconds()))
                .sorted()
                .toList();
        if (seconds.isEmpty()) {
            return null;
        }
        return seconds.get((seconds.size() - 1) / 2);
    }

    private ReachResponse getReach() {
        Map<Boolean, Long> devicesByOsPermission = statsQueryRepository.countDevicesByOsPermission();
        long devicesWithToken = devicesByOsPermission.values().stream().mapToLong(Long::longValue).sum();

        return new ReachResponse(
                statsQueryRepository.countMembersWithToken(),
                devicesWithToken,
                devicesByOsPermission.getOrDefault(Boolean.TRUE, 0L),
                devicesByOsPermission.getOrDefault(Boolean.FALSE, 0L),
                devicesByOsPermission.getOrDefault(null, 0L)
        );
    }

    private List<PlatformOpenCountResponse> getOpensByPlatform(LocalDateTime start, LocalDateTime end) {
        Map<DevicePlatform, Long> counts = statsQueryRepository.countOpensByPlatform(start, end);
        return Arrays.stream(DevicePlatform.values())
                .map(platform -> new PlatformOpenCountResponse(platform, counts.getOrDefault(platform, 0L)))
                .toList();
    }

    private Double divide(long numerator, Long denominator) {
        if (denominator == null || denominator == 0) {
            return null;
        }
        return (double) numerator / denominator;
    }
}
