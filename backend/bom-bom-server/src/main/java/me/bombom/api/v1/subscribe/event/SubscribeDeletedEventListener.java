package me.bombom.api.v1.subscribe.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.subscribe.service.NewsletterSubscriptionCountService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscribeDeletedEventListener {

    private final NewsletterSubscriptionCountService newsletterSubscriptionCountService;

    @TransactionalEventListener
    public void handle(SubscribeDeletedEvent event) {
        newsletterSubscriptionCountService.decreaseNewsletterSubscriptionCount(event.newsletterId(), event.birthDate());
    }
}
