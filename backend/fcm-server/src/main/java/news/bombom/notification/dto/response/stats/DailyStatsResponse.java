package news.bombom.notification.dto.response.stats;

import java.time.LocalDate;
import news.bombom.notification.domain.NotificationCategory;

/**
 * 발송일 하루·카테고리 하나의 발송 수와 그중 열린 수. 열림은 열린 날짜가 아니라 발송일에 귀속한다.
 *
 * @param sent   그날 생성되어 SENT가 된 회원 알림 수. 토픽 발송 카테고리는 null
 * @param opened 그날 보낸 알림 중 한 번이라도 열린 수. 토픽 발송은 (scheduleId, memberId) 단위
 * @param estimatedRecipients 토픽 발송만. 그날 발송된 스케줄의 발송 시점 수신 동의 회원 수 합. 회원별 발송은 null
 */
public record DailyStatsResponse(
        LocalDate date,
        NotificationCategory category,
        Long sent,
        long opened,
        Long estimatedRecipients
) {
}
