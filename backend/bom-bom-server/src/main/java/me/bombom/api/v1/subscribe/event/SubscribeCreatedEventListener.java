package me.bombom.api.v1.subscribe.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.subscribe.service.NewsletterSubscriptionCountService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscribeCreatedEventListener {

    private final NewsletterSubscriptionCountService newsletterSubscriptionCountService;

    @Async
    @TransactionalEventListener
    public void handle(SubscribeCreatedEvent event) {
        newsletterSubscriptionCountService.updateNewsletterSubscriptionCount(event.newsletterId(), event.birthDate());
    }
}
