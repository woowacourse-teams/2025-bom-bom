package news.bombom.inquiry.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import news.bombom.inquiry.domain.InquiryMessageArrivalNotification;
import news.bombom.inquiry.repository.InquiryMessageArrivalNotificationRepository;
import news.bombom.notification.domain.NotificationStatus;
import news.bombom.notification.dto.response.NotificationResultResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 트랜잭션 경계가 끊기는 운영 상황을 재현하기 위해, 테스트 클래스 트랜잭션 비활성화(NOT_SUPPORTED), 준영속 엔티티 세팅은 TransactionTemplate으로 별도 트랜잭션을 열어 커밋
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(InquiryMessageArrivalNotificationStatusService.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class InquiryMessageArrivalNotificationStatusServicePersistenceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InquiryMessageArrivalNotificationRepository notificationRepository;

    @Autowired
    private InquiryMessageArrivalNotificationStatusService statusService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void 발송_성공_시_SENT_상태로_DB에_저장되고_재시도_대상에서_제외된다() {
        // given
        InquiryMessageArrivalNotification notification = saveDetachedNotificationEntity(NotificationStatus.PENDING, 0);
        NotificationResultResponse result = new NotificationResultResponse(1, 0, 0, "");

        // when
        statusService.updateStatus(notification, result);

        // then
        // SENT 상태 변경 확인
        InquiryMessageArrivalNotification saved = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.SENT);

        // 재시도 대상 제외 여부 확인
        List<InquiryMessageArrivalNotification> retryCandidates = notificationRepository.findRetryCandidates(
                List.of(NotificationStatus.PENDING, NotificationStatus.FAILED), LocalDateTime.now());
        assertThat(retryCandidates).extracting(InquiryMessageArrivalNotification::getId)
                .doesNotContain(notification.getId());
    }

    @Test
    void 전체_발송_실패_시_FAILED_상태와_재시도_정보가_DB에_저장된다() {
        // given
        InquiryMessageArrivalNotification notification = saveDetachedNotificationEntity(NotificationStatus.PENDING, 0);
        NotificationResultResponse result = new NotificationResultResponse(0, 1, 0, "FCM 전송 실패");

        // when
        statusService.updateStatus(notification, result);

        // then
        InquiryMessageArrivalNotification saved = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(saved.getAttempts()).isEqualTo(1);
        assertThat(saved.getNextRetryAt()).isNotNull();
        assertThat(saved.getLastError()).isEqualTo("FCM 전송 실패");
    }

    @Test
    void 모든_기기가_스킵된_경우_SENT_상태로_DB에_저장된다() {
        // given
        InquiryMessageArrivalNotification notification = saveDetachedNotificationEntity(NotificationStatus.PENDING, 0);
        NotificationResultResponse result = new NotificationResultResponse(0, 0, 1, "");

        // when
        statusService.updateStatus(notification, result);

        // then
        InquiryMessageArrivalNotification saved = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void FCM_토큰이_없으면_markAsFailed_호출로_실패_상태와_재시도_정보가_DB에_저장된다() {
        // given
        InquiryMessageArrivalNotification notification = saveDetachedNotificationEntity(NotificationStatus.PENDING, 0);

        // when
        statusService.markAsFailed(notification, "FCM 토큰 없음");

        // then
        InquiryMessageArrivalNotification saved = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(saved.getAttempts()).isEqualTo(1);
        assertThat(saved.getNextRetryAt()).isNotNull();
        assertThat(saved.getLastError()).isEqualTo("FCM 토큰 없음");
    }

    @Test
    void 이미_읽은_알림은_삭제하지_않고_SKIPPED_상태로_DB에_저장되고_재시도_대상에서_제외된다() {
        // given
        InquiryMessageArrivalNotification notification = saveDetachedNotificationEntity(NotificationStatus.PENDING, 0);

        // when
        statusService.markAlreadyReadAsSkipped(notification, 20L);

        // then
        InquiryMessageArrivalNotification saved = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.SKIPPED);

        List<InquiryMessageArrivalNotification> retryCandidates = notificationRepository.findRetryCandidates(
                List.of(NotificationStatus.PENDING, NotificationStatus.FAILED), LocalDateTime.now());
        assertThat(retryCandidates).extracting(InquiryMessageArrivalNotification::getId)
                .doesNotContain(notification.getId());
    }

    @Test
    void 알림_수신_거부_시_DB에서_알림이_삭제된다() {
        // given
        InquiryMessageArrivalNotification notification = saveDetachedNotificationEntity(NotificationStatus.PENDING, 0);

        // when
        statusService.handleRejected(notification);

        // then
        assertThat(notificationRepository.findById(notification.getId())).isEmpty();
    }

    private InquiryMessageArrivalNotification saveDetachedNotificationEntity(NotificationStatus status, int attempts) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        return transactionTemplate.execute(statusTransaction -> {
            InquiryMessageArrivalNotification notification = InquiryMessageArrivalNotification.builder()
                    .memberId(1L)
                    .messageId(10L)
                    .status(status)
                    .attempts(attempts)
                    .build();
            notification = entityManager.persistAndFlush(notification);
            entityManager.clear();
            return notification;
        });
    }
}
