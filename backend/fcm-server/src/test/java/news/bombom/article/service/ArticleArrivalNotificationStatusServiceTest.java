package news.bombom.article.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import news.bombom.article.domain.ArticleArrivalNotification;
import news.bombom.article.domain.ArticleArrivalNotificationFailed;
import news.bombom.article.repository.ArticleArrivalNotificationFailedRepository;
import news.bombom.article.repository.ArticleArrivalNotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("아티클 알림 상태 처리 서비스 테스트")
class ArticleArrivalNotificationStatusServiceTest {

    @Mock
    private ArticleArrivalNotificationRepository notificationRepository;

    @Mock
    private ArticleArrivalNotificationFailedRepository articleArrivalNotificationFailedRepository;

    @InjectMocks
    private ArticleArrivalNotificationStatusService statusService;

    @Test
    @DisplayName("알림 수신 거부 시 재시도 여부와 무관하게 즉시 실패 테이블로 이관하고 원본을 삭제한다")
    void handleRejected_MovesToFailedTableAndDeletesOriginal() {
        // Given
        ArticleArrivalNotification notification = ArticleArrivalNotification.builder()
                .memberId(1L)
                .articleId(123L)
                .articleTitle("테스트 제목")
                .newsletterName("테스트 뉴스레터")
                .build();
        ReflectionTestUtils.setField(notification, "id", 1L);

        // When
        statusService.handleRejected(notification);

        // Then
        verify(articleArrivalNotificationFailedRepository, times(1)).save(any(ArticleArrivalNotificationFailed.class));
        verify(notificationRepository, times(1)).delete(notification);
    }
}
