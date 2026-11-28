package news.bombom.notification.dto.response.stats;

import java.util.List;
import java.util.Map;
import news.bombom.notification.domain.NotificationCategory;
import news.bombom.notification.domain.NotificationStatus;

/**
 * statusCounts, deviceSends는 토픽 발송(TOPIC)이면 null이다.
 * openRate는 MEMBER면 uniqueLinked ÷ SENT, TOPIC이면 uniqueLinked ÷ estimatedRecipients(근사)이고 분모가 0이면 null이다.
 *
 * @param consentingMembers   조회 시점의 수신 동의 회원 수
 * @param estimatedRecipients TOPIC만. 기간에 발송된 스케줄의 발송 시점 수신 동의 회원 수 합. MEMBER는 null
 */
public record CategoryStatsResponse(
        NotificationCategory category,
        DeliveryMode deliveryMode,
        Map<NotificationStatus, Long> statusCounts,
        List<FailureReasonResponse> failureReasons,
        DeviceSendsResponse deviceSends,
        OpensResponse opens,
        Double openRate,
        Long medianSecondsToOpen,
        long consentingMembers,
        Long estimatedRecipients
) {

    public enum DeliveryMode {
        MEMBER,
        TOPIC
    }

    public record FailureReasonResponse(String reason, long count) {
    }

    public record DeviceSendsResponse(long success, long failure) {
    }

    public record OpensResponse(long linked, long uniqueLinked, long unlinked) {
    }
}
