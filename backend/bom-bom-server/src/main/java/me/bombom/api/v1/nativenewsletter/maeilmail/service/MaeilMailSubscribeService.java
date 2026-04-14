package me.bombom.api.v1.nativenewsletter.maeilmail.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSubscription;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSubscriptionTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailSubscribeRequest;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailSubscriptionRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailSubscriptionTrackRepository;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.api.v1.subscribe.domain.Subscribe;
import me.bombom.api.v1.subscribe.repository.SubscribeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaeilMailSubscribeService {

    private final SubscribeRepository subscribeRepository;
    private final NewsletterRepository newsletterRepository;
    private final MaeilMailSubscriptionRepository maeilMailSubscriptionRepository;
    private final MaeilMailSubscriptionTrackRepository maeilMailSubscriptionTrackRepository;

    @Transactional
    public void subscribe(Long memberId, MaeilMailSubscribeRequest request) {
        Newsletter newsletter = newsletterRepository.findById(request.newsletterId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "newsletter")
                        .addContext("newsletterId", request.newsletterId()));

        if (!newsletter.isNative()) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                    .addContext(ErrorContextKeys.NEWSLETTER_ID, request.newsletterId())
                    .addContext(ErrorContextKeys.DETAIL, "봄봄 자체 뉴스레터만 해당 API를 통해 구독할 수 있습니다.");
        }

        if (subscribeRepository.existsByMemberIdAndNewsletterId(memberId, request.newsletterId())) {
            throw new CIllegalArgumentException(ErrorDetail.DUPLICATED_DATA)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "subscribe")
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.NEWSLETTER_ID, request.newsletterId());
        }

        Subscribe subscribe = subscribeRepository.save(Subscribe.builder()
                .memberId(memberId)
                .newsletterId(request.newsletterId())
                .build());

        MaeilMailSubscription subscription = maeilMailSubscriptionRepository.save(
                MaeilMailSubscription.builder()
                        .subscribeId(subscribe.getId())
                        .memberId(memberId)
                        .weeklyIssueCount(request.weeklyIssueCount())
                        .build()
        );

        request.tracks().forEach(track ->
                maeilMailSubscriptionTrackRepository.save(
                        MaeilMailSubscriptionTrack.builder()
                                .maeilMailSubscriptionId(subscription.getId())
                                .field(track)
                                .build()
                )
        );
    }
}
