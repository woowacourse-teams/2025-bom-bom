package news.bombom.notification.repository;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import news.bombom.notification.domain.DevicePlatform;
import news.bombom.notification.domain.NotificationCategory;
import news.bombom.notification.domain.NotificationStatus;
import news.bombom.notification.domain.SendResult.SendStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 어드민 알림 지표 집계 쿼리. 기간은 [start, end) 범위다.
 */
@Repository
@RequiredArgsConstructor
public class NotificationStatsQueryRepository {

    private static final int IN_CLAUSE_CHUNK_SIZE = 1000;

    /**
     * (scheduleId, memberId) 조합을 COUNT(DISTINCT)로 세기 위한 문자열 키. memberId가 없는 열림은 0으로 묶는다.
     */
    private static final String SCHEDULE_MEMBER_KEY =
            "CONCAT(STR(o.scheduleId), ':', STR(COALESCE(o.memberId, 0)))";

    /**
     * 회원별 발송 카테고리의 알림 row 엔티티 이름. 쿼리 문자열에 들어가므로 상수로만 관리한다.
     */
    private static final Map<NotificationCategory, String> MEMBER_NOTIFICATION_ENTITIES = Map.of(
            NotificationCategory.ARTICLE, "ArticleArrivalNotification",
            NotificationCategory.CHALLENGE_START, "ChallengeStartNotification",
            NotificationCategory.CHALLENGE_TODO_REMINDER, "ChallengeTodoReminderNotification",
            NotificationCategory.INQUIRY_MESSAGE_ARRIVAL, "InquiryMessageArrivalNotification"
    );

    private final EntityManager entityManager;

    public Map<NotificationStatus, Long> countByStatus(NotificationCategory category,
                                                       LocalDateTime start, LocalDateTime end) {
        List<Object[]> rows = entityManager.createQuery(
                        "SELECT n.status, COUNT(n) FROM " + entityName(category) + " n"
                                + " WHERE n.createdAt >= :start AND n.createdAt < :end"
                                + " GROUP BY n.status", Object[].class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();

        Map<NotificationStatus, Long> counts = new EnumMap<>(NotificationStatus.class);
        rows.forEach(row -> counts.put((NotificationStatus) row[0], (Long) row[1]));
        return counts;
    }

    public long countByLastError(NotificationCategory category, String lastError,
                                 LocalDateTime start, LocalDateTime end) {
        return entityManager.createQuery(
                        "SELECT COUNT(n) FROM " + entityName(category) + " n"
                                + " WHERE n.lastError = :lastError"
                                + " AND n.status IN (:statuses)"
                                + " AND n.createdAt >= :start AND n.createdAt < :end", Long.class)
                .setParameter("lastError", lastError)
                .setParameter("statuses", List.of(NotificationStatus.FAILED, NotificationStatus.DEAD))
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();
    }

    /**
     * 재시도를 소진해 실패 테이블로 옮겨진 아티클 알림 수. 원본 row는 삭제되므로 DEAD 집계에 더한다.
     */
    public long countMovedArticleFailures(LocalDateTime start, LocalDateTime end) {
        return entityManager.createQuery(
                        "SELECT COUNT(f) FROM ArticleArrivalNotificationFailed f"
                                + " WHERE f.createdAt >= :start AND f.createdAt < :end", Long.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();
    }

    public long countMovedArticleFailuresByLastError(String lastError, LocalDateTime start, LocalDateTime end) {
        return entityManager.createQuery(
                        "SELECT COUNT(f) FROM ArticleArrivalNotificationFailed f"
                                + " WHERE f.lastError = :lastError"
                                + " AND f.createdAt >= :start AND f.createdAt < :end", Long.class)
                .setParameter("lastError", lastError)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();
    }

    public Map<SendStatus, Long> countDeviceSendsByStatus(NotificationCategory category,
                                                          LocalDateTime start, LocalDateTime end) {
        List<Object[]> rows = entityManager.createQuery(
                        "SELECT l.status, COUNT(l) FROM NotificationDeviceSendLog l"
                                + " WHERE l.category = :category AND l.sentAt >= :start AND l.sentAt < :end"
                                + " GROUP BY l.status", Object[].class)
                .setParameter("category", category)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();

        Map<SendStatus, Long> counts = new EnumMap<>(SendStatus.class);
        rows.forEach(row -> counts.put((SendStatus) row[0], (Long) row[1]));
        return counts;
    }

    /**
     * 기기별 발송 실패를 에러 코드별로 센다. 에러 코드가 없으면 null 키로 묶인다.
     */
    public Map<String, Long> countDeviceFailuresByErrorCode(NotificationCategory category,
                                                            LocalDateTime start, LocalDateTime end) {
        List<Object[]> rows = entityManager.createQuery(
                        "SELECT l.errorCode, COUNT(l) FROM NotificationDeviceSendLog l"
                                + " WHERE l.category = :category AND l.status = :status"
                                + " AND l.sentAt >= :start AND l.sentAt < :end"
                                + " GROUP BY l.errorCode", Object[].class)
                .setParameter("category", category)
                .setParameter("status", SendStatus.FAILED)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();

        Map<String, Long> counts = new HashMap<>();
        rows.forEach(row -> counts.put((String) row[0], (Long) row[1]));
        return counts;
    }

    public long countLinkedOpens(NotificationCategory category, LocalDateTime start, LocalDateTime end) {
        return entityManager.createQuery(
                        "SELECT COUNT(o) FROM NotificationOpen o"
                                + " WHERE o.category = :category"
                                + " AND (o.notificationId IS NOT NULL OR o.scheduleId IS NOT NULL)"
                                + " AND o.openedAt >= :start AND o.openedAt < :end", Long.class)
                .setParameter("category", category)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();
    }

    public long countUnlinkedOpens(NotificationCategory category, LocalDateTime start, LocalDateTime end) {
        return entityManager.createQuery(
                        "SELECT COUNT(o) FROM NotificationOpen o"
                                + " WHERE o.category = :category"
                                + " AND o.notificationId IS NULL AND o.scheduleId IS NULL"
                                + " AND o.openedAt >= :start AND o.openedAt < :end", Long.class)
                .setParameter("category", category)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();
    }

    /**
     * 회원별 발송 알림의 첫 열림 시각. key = 알림 row id. 실제 알림 row가 없는 열림은 제외한다.
     */
    public Map<Long, LocalDateTime> findFirstOpenedAtByNotificationId(NotificationCategory category,
                                                                      LocalDateTime start, LocalDateTime end) {
        List<Object[]> rows = entityManager.createQuery(
                        "SELECT o.notificationId, MIN(o.openedAt)"
                                + " FROM NotificationOpen o, " + entityName(category) + " n"
                                + " WHERE o.notificationId = n.id AND o.category = :category"
                                + " AND o.openedAt >= :start AND o.openedAt < :end"
                                + " GROUP BY o.notificationId", Object[].class)
                .setParameter("category", category)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();

        Map<Long, LocalDateTime> firstOpenedAt = new HashMap<>();
        rows.forEach(row -> firstOpenedAt.put((Long) row[0], (LocalDateTime) row[1]));
        return firstOpenedAt;
    }

    /**
     * 토픽 발송 알림을 연 (scheduleId, memberId) 조합 수. memberId가 없는 열림은 스케줄마다 하나로 묶인다.
     * 실제 스케줄이 없는 열림은 제외하고, 조합을 애플리케이션에 올리지 않도록 DB에서 센다.
     */
    public long countDistinctScheduleMemberOpens(NotificationCategory category,
                                                 LocalDateTime start, LocalDateTime end) {
        return entityManager.createQuery(
                        "SELECT COUNT(DISTINCT " + SCHEDULE_MEMBER_KEY + ")"
                                + " FROM NotificationOpen o, EventNotificationSchedule s"
                                + " WHERE o.scheduleId = s.id AND o.category = :category"
                                + " AND o.openedAt >= :start AND o.openedAt < :end", Long.class)
                .setParameter("category", category)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();
    }

    /**
     * 발송일별 SENT 알림 수. key = 알림 row 생성일
     */
    public Map<LocalDate, Long> countSentBySendDate(NotificationCategory category,
                                                    LocalDateTime start, LocalDateTime end) {
        List<Object[]> rows = entityManager.createQuery(
                        "SELECT CAST(n.createdAt AS LocalDate), COUNT(n) FROM " + entityName(category) + " n"
                                + " WHERE n.status = :status AND n.createdAt >= :start AND n.createdAt < :end"
                                + " GROUP BY CAST(n.createdAt AS LocalDate)", Object[].class)
                .setParameter("status", NotificationStatus.SENT)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
        return toDateCounts(rows);
    }

    /**
     * 발송일별로, 그날 보낸 SENT 알림 중 한 번이라도 열린 알림 수. 열림 시각은 기간에 제한하지 않는다.
     */
    public Map<LocalDate, Long> countOpenedNotificationsBySendDate(NotificationCategory category,
                                                                   LocalDateTime start, LocalDateTime end) {
        List<Object[]> rows = entityManager.createQuery(
                        "SELECT CAST(n.createdAt AS LocalDate), COUNT(DISTINCT n.id)"
                                + " FROM " + entityName(category) + " n, NotificationOpen o"
                                + " WHERE o.notificationId = n.id AND o.category = :category"
                                + " AND n.status = :status AND n.createdAt >= :start AND n.createdAt < :end"
                                + " GROUP BY CAST(n.createdAt AS LocalDate)", Object[].class)
                .setParameter("category", category)
                .setParameter("status", NotificationStatus.SENT)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
        return toDateCounts(rows);
    }

    /**
     * 토픽 발송 스케줄의 실제 발송일별로, 그 스케줄 알림을 연 (scheduleId, memberId) 조합 수.
     * memberId가 없는 열림은 스케줄마다 하나로 묶인다.
     */
    public Map<LocalDate, Long> countDistinctScheduleMemberOpensBySendDate(NotificationCategory category,
                                                                           LocalDateTime start,
                                                                           LocalDateTime end) {
        List<Object[]> rows = entityManager.createQuery(
                        "SELECT CAST(s.sentAt AS LocalDate), COUNT(DISTINCT " + SCHEDULE_MEMBER_KEY + ")"
                                + " FROM EventNotificationSchedule s, NotificationOpen o"
                                + " WHERE o.scheduleId = s.id AND o.category = :category"
                                + " AND s.sent = true AND s.sentAt >= :start AND s.sentAt < :end"
                                + " GROUP BY CAST(s.sentAt AS LocalDate)", Object[].class)
                .setParameter("category", category)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
        return toDateCounts(rows);
    }

    /**
     * 기간에 발송된 이벤트 스케줄의 발송 시점 수신 동의 회원 수를 발송일별로 더한다.
     * 값이 기록되기 전에 발송된 스케줄은 fallback(조회 시점 동의 회원 수)으로 대신한다.
     */
    public Map<LocalDate, Long> sumEventConsentingMembersBySendDate(LocalDateTime start, LocalDateTime end,
                                                                    long fallback) {
        List<Object[]> rows = entityManager.createQuery(
                        "SELECT CAST(s.sentAt AS LocalDate), s.consentingMemberCount FROM EventNotificationSchedule s"
                                + " WHERE s.sent = true AND s.sentAt >= :start AND s.sentAt < :end", Object[].class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();

        Map<LocalDate, Long> sums = new HashMap<>();
        rows.forEach(row -> sums.merge((LocalDate) row[0],
                row[1] == null ? fallback : ((Integer) row[1]).longValue(), Long::sum));
        return sums;
    }

    public Map<Long, LocalDateTime> findCreatedAtByIds(NotificationCategory category, Collection<Long> ids) {
        Map<Long, LocalDateTime> createdAtById = new HashMap<>();
        List<Long> idList = new ArrayList<>(ids);
        for (int from = 0; from < idList.size(); from += IN_CLAUSE_CHUNK_SIZE) {
            List<Long> chunk = idList.subList(from, Math.min(from + IN_CLAUSE_CHUNK_SIZE, idList.size()));
            entityManager.createQuery(
                            "SELECT n.id, n.createdAt FROM " + entityName(category) + " n WHERE n.id IN (:ids)",
                            Object[].class)
                    .setParameter("ids", chunk)
                    .getResultList()
                    .forEach(row -> createdAtById.put((Long) row[0], (LocalDateTime) row[1]));
        }
        return createdAtById;
    }

    /**
     * 토큰을 가진 회원 중 해당 카테고리 수신에 동의한 회원 수. 설정 row가 없으면 카테고리 기본값을 따른다.
     */
    public long countConsentingMembers(NotificationCategory category) {
        return entityManager.createQuery(
                        "SELECT COUNT(DISTINCT t.memberId) FROM MemberFcmToken t"
                                + " WHERE EXISTS (SELECT 1 FROM MemberNotificationSetting s"
                                + "   WHERE s.memberId = t.memberId AND s.category = :category AND s.isEnabled = true)"
                                + " OR (:defaultSetting = true AND NOT EXISTS (SELECT 1 FROM MemberNotificationSetting s2"
                                + "   WHERE s2.memberId = t.memberId AND s2.category = :category))", Long.class)
                .setParameter("category", category)
                .setParameter("defaultSetting", category.getDefaultSetting())
                .getSingleResult();
    }

    /**
     * 발송 트랜잭션 안에서 쿼리가 실패하면 트랜잭션이 롤백 대상으로 표시돼 발송 완료 기록까지 롤백된다.
     * 발송 중 지표용으로 셀 때는 별도 트랜잭션에서 센다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public long countConsentingMembersInNewTransaction(NotificationCategory category) {
        return countConsentingMembers(category);
    }

    public long countMembersWithToken() {
        return entityManager.createQuery("SELECT COUNT(DISTINCT t.memberId) FROM MemberFcmToken t", Long.class)
                .getSingleResult();
    }

    /**
     * OS 알림 권한별 기기 수. key는 true(허용) / false(거부) / null(미보고)
     */
    public Map<Boolean, Long> countDevicesByOsPermission() {
        List<Object[]> rows = entityManager.createQuery(
                        "SELECT t.osPermissionGranted, COUNT(t) FROM MemberFcmToken t"
                                + " GROUP BY t.osPermissionGranted", Object[].class)
                .getResultList();

        Map<Boolean, Long> counts = new HashMap<>();
        rows.forEach(row -> counts.put((Boolean) row[0], (Long) row[1]));
        return counts;
    }

    public Map<DevicePlatform, Long> countOpensByPlatform(LocalDateTime start, LocalDateTime end) {
        List<Object[]> rows = entityManager.createQuery(
                        "SELECT o.platform, COUNT(o) FROM NotificationOpen o"
                                + " WHERE o.openedAt >= :start AND o.openedAt < :end"
                                + " GROUP BY o.platform", Object[].class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();

        Map<DevicePlatform, Long> counts = new EnumMap<>(DevicePlatform.class);
        rows.forEach(row -> counts.put((DevicePlatform) row[0], (Long) row[1]));
        return counts;
    }

    private Map<LocalDate, Long> toDateCounts(List<Object[]> rows) {
        Map<LocalDate, Long> counts = new HashMap<>();
        rows.forEach(row -> counts.put((LocalDate) row[0], (Long) row[1]));
        return counts;
    }

    private String entityName(NotificationCategory category) {
        String entityName = MEMBER_NOTIFICATION_ENTITIES.get(category);
        if (entityName == null) {
            throw new IllegalArgumentException("회원별 발송 카테고리가 아닙니다: " + category);
        }
        return entityName;
    }
}
