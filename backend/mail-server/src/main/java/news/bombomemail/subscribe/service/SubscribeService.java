package news.bombomemail.subscribe.service;

import lombok.RequiredArgsConstructor;
import news.bombomemail.subscribe.domain.Subscribe;
import news.bombomemail.subscribe.event.NewsletterSubscribedEvent;
import news.bombomemail.subscribe.event.UnsubscribeUrlMissingEvent;
import news.bombomemail.subscribe.repository.SubscribeRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscribeService {

    private final SubscribeRepository subscribeRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void upsertSubscribe(
            Long newsletterId,
            Long memberId,
            String unsubscribeUrl,
            String newsletterName,
            String articleTitle
    ) {
        Subscribe subscribe = subscribeRepository.findByMemberIdAndNewsletterId(memberId, newsletterId)
                .orElseGet(() -> registerSubscribe(newsletterId, memberId, unsubscribeUrl));
        subscribe.updateUnsubscribeUrl(unsubscribeUrl);

        if (subscribe.isUnsubscribeUrlMissing()) {
            applicationEventPublisher.publishEvent(
                    new UnsubscribeUrlMissingEvent(
                            newsletterId,
                            newsletterName,
                            articleTitle,
                            memberId
                    )
            );
        }
    }

    private Subscribe registerSubscribe(Long newsletterId, Long memberId, String unsubscribeUrl) {
        Subscribe newSubscribe = Subscribe.builder()
                .newsletterId(newsletterId)
                .memberId(memberId)
                .unsubscribeUrl(unsubscribeUrl)
                .build();

        applicationEventPublisher.publishEvent(
                NewsletterSubscribedEvent.of(newsletterId, memberId)
        );
        return subscribeRepository.save(newSubscribe);
    }
}
