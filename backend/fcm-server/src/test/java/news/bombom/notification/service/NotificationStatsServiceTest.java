package news.bombom.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import news.bombom.article.domain.ArticleArrivalNotification;
import news.bombom.article.domain.ArticleArrivalNotificationFailed;
import news.bombom.notification.domain.DevicePlatform;
import news.bombom.notification.domain.MemberFcmToken;
import news.bombom.notification.domain.MemberNotificationSetting;
import news.bombom.notification.domain.NotificationCategory;
import news.bombom.notification.domain.NotificationDeviceSendLog;
import news.bombom.notification.domain.NotificationOpen;
import news.bombom.notification.domain.NotificationStatus;
import news.bombom.notification.domain.SendResult.SendStatus;
import news.bombom.notification.dto.response.stats.CategoryStatsResponse;
import news.bombom.notification.dto.response.stats.CategoryStatsResponse.DeliveryMode;
import news.bombom.notification.dto.response.stats.CategoryStatsResponse.FailureReasonResponse;
import news.bombom.notification.dto.response.stats.NotificationStatsResponse;
import news.bombom.notification.dto.response.stats.PlatformOpenCountResponse;
import news.bombom.notification.repository.NotificationStatsQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.server.ResponseStatusException;

@DataJpaTest
@ActiveProfiles("test")
@Import({NotificationStatsService.class, NotificationStatsQueryRepository.class})
@Sql("/sql/challenge-todo-reminder-notification-h2.sql")
@DisplayName("어드민 알림 지표 집계 서비스 테스트")
class NotificationStatsServiceTest {

    private static final long MEMBER_A = 12L;
    private static final long MEMBER_B = 13L;
    private static final long SCHEDULE_ID = 7L;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NotificationStatsService statsService;

    private final LocalDate today = LocalDate.now();
    private ArticleArrivalNotification openedArticle;

    @BeforeEach
    void setUp() {
        openedArticle = persistArticle(NotificationStatus.SENT, null);
        persistArticle(NotificationStatus.SENT, null);
        persistArticle(NotificationStatus.FAILED, NotificationProcessingService.NO_TOKEN_ERROR);
        entityManager.persist(ArticleArrivalNotificationFailed.builder()
                .originalNotificationId(999L)
                .memberId(MEMBER_A)
                .articleId(1L)
                .newsletterName("봄봄레터")
                .articleTitle("제목")
                .finalAttempts(7)
                .lastError("Device x: FCM 발송 실패")
                .build());

        LocalDateTime articleCreatedAt = openedArticle.getCreatedAt();
        persistOpen(NotificationCategory.ARTICLE, openedArticle.getId(), null, MEMBER_A, DevicePlatform.IOS,
                articleCreatedAt.plusSeconds(60));
        persistOpen(NotificationCategory.ARTICLE, openedArticle.getId(), null, MEMBER_A, DevicePlatform.IOS,
                articleCreatedAt.plusSeconds(120));
        persistOpen(NotificationCategory.ARTICLE, null, null, null, DevicePlatform.ANDROID, LocalDateTime.now());
        persistOpen(NotificationCategory.EVENT, null, SCHEDULE_ID, MEMBER_A, DevicePlatform.ANDROID, LocalDateTime.now());
        persistOpen(NotificationCategory.EVENT, null, SCHEDULE_ID, MEMBER_A, DevicePlatform.ANDROID, LocalDateTime.now());
        persistOpen(NotificationCategory.EVENT, null, SCHEDULE_ID, MEMBER_B, DevicePlatform.IOS, LocalDateTime.now());
        persistOpen(null, null, null, null, DevicePlatform.ANDROID, LocalDateTime.now());
        persistOpen(NotificationCategory.ARTICLE, openedArticle.getId(), null, MEMBER_A, DevicePlatform.IOS,
                today.minusDays(10).atStartOfDay());

        persistDeviceLog(SendStatus.SUCCESS, null);
        persistDeviceLog(SendStatus.SUCCESS, null);
        persistDeviceLog(SendStatus.SUCCESS, null);
        persistDeviceLog(SendStatus.FAILED, "UNREGISTERED");
        persistDeviceLog(SendStatus.SKIPPED, null);

        persistToken(MEMBER_A, "device-a1", true);
        persistToken(MEMBER_A, "device-a2", null);
        persistToken(MEMBER_B, "device-b1", false);
        persistSetting(MEMBER_A, NotificationCategory.EVENT, true);
        persistSetting(MEMBER_B, NotificationCategory.ARTICLE, false);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void 회원별_발송_카테고리는_상태별_발송수와_건단위_열림을_집계한다() {
        NotificationStatsResponse response = statsService.getStats(today.minusDays(1), today.plusDays(1));

        CategoryStatsResponse article = findCategory(response, NotificationCategory.ARTICLE);
        assertThat(article.deliveryMode()).isEqualTo(DeliveryMode.MEMBER);
        assertThat(article.statusCounts())
                .containsEntry(NotificationStatus.SENT, 2L)
                .containsEntry(NotificationStatus.FAILED, 1L)
                .containsEntry(NotificationStatus.DEAD, 1L)
                .containsEntry(NotificationStatus.PENDING, 0L)
                .containsEntry(NotificationStatus.SKIPPED, 0L);
        assertThat(article.failureReasons()).containsExactlyInAnyOrder(
                new FailureReasonResponse(NotificationProcessingService.NO_TOKEN_ERROR, 1L),
                new FailureReasonResponse("UNREGISTERED", 1L));
        assertThat(article.deviceSends().success()).isEqualTo(3L);
        assertThat(article.deviceSends().failure()).isEqualTo(1L);
        assertThat(article.opens().linked()).isEqualTo(2L);
        assertThat(article.opens().uniqueLinked()).isEqualTo(1L);
        assertThat(article.opens().unlinked()).isEqualTo(1L);
        assertThat(article.openRate()).isEqualTo(0.5);
        assertThat(article.medianSecondsToOpen()).isEqualTo(60L);
        assertThat(article.consentingMembers()).isEqualTo(1L);
    }

    @Test
    void 토픽_발송_카테고리는_스케줄_회원_단위로_열림을_세고_동의_회원수를_분모로_쓴다() {
        NotificationStatsResponse response = statsService.getStats(today.minusDays(1), today.plusDays(1));

        CategoryStatsResponse event = findCategory(response, NotificationCategory.EVENT);
        assertThat(event.deliveryMode()).isEqualTo(DeliveryMode.TOPIC);
        assertThat(event.statusCounts()).isNull();
        assertThat(event.deviceSends()).isNull();
        assertThat(event.opens().linked()).isEqualTo(3L);
        assertThat(event.opens().uniqueLinked()).isEqualTo(2L);
        assertThat(event.consentingMembers()).isEqualTo(1L);
        assertThat(event.openRate()).isEqualTo(2.0);
        assertThat(event.medianSecondsToOpen()).isNull();
    }

    @Test
    void 데이터가_없는_카테고리도_포함하고_분모가_0이면_열림률은_null이다() {
        NotificationStatsResponse response = statsService.getStats(today.minusDays(1), today.plusDays(1));

        assertThat(response.categories()).hasSize(NotificationCategory.values().length);
        CategoryStatsResponse inquiry = findCategory(response, NotificationCategory.INQUIRY_MESSAGE_ARRIVAL);
        assertThat(inquiry.statusCounts()).containsEntry(NotificationStatus.SENT, 0L);
        assertThat(inquiry.openRate()).isNull();
        assertThat(inquiry.medianSecondsToOpen()).isNull();
    }

    @Test
    void 도달_모수와_OS별_열림을_집계한다() {
        NotificationStatsResponse response = statsService.getStats(today.minusDays(1), today.plusDays(1));

        assertThat(response.reach().membersWithToken()).isEqualTo(2L);
        assertThat(response.reach().devicesWithToken()).isEqualTo(3L);
        assertThat(response.reach().devicesOsPermissionGranted()).isEqualTo(1L);
        assertThat(response.reach().devicesOsPermissionDenied()).isEqualTo(1L);
        assertThat(response.reach().devicesOsPermissionUnknown()).isEqualTo(1L);
        assertThat(response.opensByPlatform()).containsExactly(
                new PlatformOpenCountResponse(DevicePlatform.IOS, 3L),
                new PlatformOpenCountResponse(DevicePlatform.ANDROID, 4L));
    }

    @Test
    void to가_from보다_이전이면_예외가_발생한다() {
        assertThatThrownBy(() -> statsService.getStats(today, today.minusDays(1)))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void 조회_기간이_90일을_넘으면_예외가_발생한다() {
        assertThatThrownBy(() -> statsService.getStats(today.minusDays(90), today))
                .isInstanceOf(ResponseStatusException.class);
    }

    private CategoryStatsResponse findCategory(NotificationStatsResponse response, NotificationCategory category) {
        return response.categories().stream()
                .filter(stats -> stats.category() == category)
                .findFirst()
                .orElseThrow();
    }

    private ArticleArrivalNotification persistArticle(NotificationStatus status, String lastError) {
        return entityManager.persist(ArticleArrivalNotification.builder()
                .memberId(MEMBER_A)
                .articleId(1L)
                .newsletterName("봄봄레터")
                .articleTitle("제목")
                .status(status)
                .lastError(lastError)
                .build());
    }

    private void persistOpen(NotificationCategory category, Long notificationId, Long scheduleId, Long memberId,
                             DevicePlatform platform, LocalDateTime openedAt) {
        entityManager.persist(NotificationOpen.builder()
                .category(category)
                .notificationId(notificationId)
                .scheduleId(scheduleId)
                .memberId(memberId)
                .platform(platform)
                .openedAt(openedAt)
                .build());
    }

    private void persistDeviceLog(SendStatus status, String errorCode) {
        entityManager.persist(NotificationDeviceSendLog.builder()
                .category(NotificationCategory.ARTICLE)
                .notificationId(openedArticle.getId())
                .memberId(MEMBER_A)
                .deviceUuid("device")
                .status(status)
                .errorCode(errorCode)
                .sentAt(LocalDateTime.now())
                .build());
    }

    private void persistToken(long memberId, String deviceUuid, Boolean osPermissionGranted) {
        MemberFcmToken token = MemberFcmToken.builder()
                .memberId(memberId)
                .deviceUuid(deviceUuid)
                .fcmToken("token-" + deviceUuid)
                .isNotificationEnabled(true)
                .build();
        if (osPermissionGranted != null) {
            token.updateOsPermission(osPermissionGranted);
        }
        entityManager.persist(token);
    }

    private void persistSetting(long memberId, NotificationCategory category, boolean enabled) {
        entityManager.persist(MemberNotificationSetting.builder()
                .memberId(memberId)
                .category(category)
                .isEnabled(enabled)
                .build());
    }
}
