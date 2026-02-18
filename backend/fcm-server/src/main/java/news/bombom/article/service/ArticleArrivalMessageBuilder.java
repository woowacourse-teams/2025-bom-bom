package news.bombom.article.service;

import java.util.Map;
import news.bombom.article.domain.ArticleArrivalNotification;
import news.bombom.notification.domain.MemberFcmToken;
import news.bombom.notification.domain.Notification;
import news.bombom.notification.domain.NotificationPayloadType;
import news.bombom.notification.domain.NotificationType;
import news.bombom.notification.dto.NotificationMessage;
import news.bombom.notification.service.NotificationMessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class ArticleArrivalMessageBuilder implements NotificationMessageBuilder {

    @Override
    public boolean supports(Notification notification) {
        return notification instanceof ArticleArrivalNotification;
    }

    @Override
    public NotificationMessage build(Notification notification, MemberFcmToken token) {
        ArticleArrivalNotification article = (ArticleArrivalNotification) notification;

        return NotificationMessage.builder()
                .recipient(token.getFcmToken())
                .title(article.getNewsletterName())
                .content(article.getArticleTitle())
                .type(NotificationType.FCM)
                .data(Map.of(
                        "articleId", String.valueOf(article.getArticleId()),
                        "notificationType", NotificationPayloadType.ARTICLE
                ))
                .build();
    }
}
