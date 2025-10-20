package news.bombomemail.subscribe.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import news.bombomemail.subscribe.domain.Subscribe;
import news.bombomemail.subscribe.event.NewsletterSubscribedEvent;
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
    public void save(Long newsletterId, Long memberId, String unsubscribeUrl) {
        subscribeRepository.findByMemberIdAndNewsletterId(memberId, newsletterId)
                .ifPresentOrElse(
                        subscribe -> subscribe.updateUnsubscribeUrl(unsubscribeUrl),
                        () -> {
                            Subscribe newSubscribe = Subscribe.builder()
                                    .newsletterId(newsletterId)
                                    .memberId(memberId)
                                    .unsubscribeUrl(unsubscribeUrl)
                                    .build();
                            subscribeRepository.save(newSubscribe);
                            applicationEventPublisher.publishEvent(
                                    NewsletterSubscribedEvent.of(newsletterId, memberId)
                            );
                        }
                );
    }
}
