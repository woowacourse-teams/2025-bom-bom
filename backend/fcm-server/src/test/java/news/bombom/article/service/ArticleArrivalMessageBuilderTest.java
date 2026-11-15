package news.bombom.article.service;

import static org.assertj.core.api.Assertions.assertThat;

import news.bombom.article.domain.ArticleArrivalNotification;
import news.bombom.notification.domain.MemberFcmToken;
import news.bombom.notification.domain.NotificationCategory;
import news.bombom.notification.dto.NotificationMessage;
import org.junit.jupiter.api.Test;

class ArticleArrivalMessageBuilderTest {

    private final ArticleArrivalMessageBuilder builder = new ArticleArrivalMessageBuilder();

    @Test
    void 아티클_카테고리로_메시지를_생성한다() {
        ArticleArrivalNotification notification = ArticleArrivalNotification.builder()
                .memberId(1L)
                .articleId(10L)
                .newsletterName("봄봄레터")
                .articleTitle("아티클 제목")
                .build();
        MemberFcmToken token = MemberFcmToken.builder()
                .memberId(1L)
                .deviceUuid("device-uuid")
                .fcmToken("fcm-token-value")
                .isNotificationEnabled(true)
                .build();

        NotificationMessage result = builder.build(notification, token);

        assertThat(result.getCategory()).isEqualTo(NotificationCategory.ARTICLE);
    }
}
