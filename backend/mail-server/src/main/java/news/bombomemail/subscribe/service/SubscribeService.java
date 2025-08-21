package news.bombomemail.subscribe.service;

import lombok.RequiredArgsConstructor;
import news.bombomemail.subscribe.domain.Subscribe;
import news.bombomemail.subscribe.event.NewsletterSubscriptionCountEvent;
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
    public void save(Long newsletterId, Long memberId) {
        boolean isSubscribe = subscribeRepository.existsByNewsletterIdAndMemberId(newsletterId, memberId);
        if (isSubscribe) {
            return;
        }

        Subscribe subscribe = Subscribe.builder()
                .newsletterId(newsletterId)
                .memberId(memberId)
                .build();
        subscribeRepository.save(subscribe);
        applicationEventPublisher.publishEvent(NewsletterSubscriptionCountEvent.of(newsletterId, memberId));
    }
}
