package me.bombom.api.v1.nativenewsletter.maeilmail.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSubscription;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSubscriptionTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTrack;
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
        Newsletter newsletter = getMaeilMailNewsletter(request.newsletterId());
        validateNotSubscribed(memberId, request.newsletterId());
        validateTracks(request);

        Subscribe subscribe = subscribeRepository.save(Subscribe.builder()
                .memberId(memberId)
                .newsletterId(newsletter.getId())
                .build());

        MaeilMailSubscription subscription = maeilMailSubscriptionRepository.save(
                MaeilMailSubscription.builder()
                        .subscribeId(subscribe.getId())
                        .memberId(memberId)
                        .weeklyIssueCount(request.weeklyIssueCount())
                        .build()
        );

        maeilMailSubscriptionTrackRepository.saveAll(buildSubscriptionTracks(subscription.getId(), request.tracks()));
    }

    private Newsletter getMaeilMailNewsletter(Long newsletterId) {
        Newsletter newsletter = newsletterRepository.findById(newsletterId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "newsletter")
                        .addContext("newsletterId", newsletterId));

        if (!newsletter.isMaeilMail()) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                    .addContext(ErrorContextKeys.NEWSLETTER_ID, newsletterId)
                    .addContext(ErrorContextKeys.DETAIL, "매일메일 뉴스레터만 해당 API를 통해 구독할 수 있습니다.");
        }

        return newsletter;
    }

    private void validateNotSubscribed(Long memberId, Long newsletterId) {
        if (subscribeRepository.existsByMemberIdAndNewsletterId(memberId, newsletterId)) {
            throw new CIllegalArgumentException(ErrorDetail.DUPLICATED_DATA)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "subscribe")
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.NEWSLETTER_ID, newsletterId);
        }
    }

    private void validateTracks(MaeilMailSubscribeRequest request) {
        long distinctTrackCount = request.tracks().stream()
                .distinct()
                .count();

        if (distinctTrackCount != request.tracks().size()) {
            throw new CIllegalArgumentException(ErrorDetail.DUPLICATED_DATA)
                    .addContext(ErrorContextKeys.OPERATION, "validateTracks")
                    .addContext(ErrorContextKeys.DETAIL, "중복된 트랙은 선택할 수 없습니다.");
        }
    }

    private List<MaeilMailSubscriptionTrack> buildSubscriptionTracks(
            Long maeilMailSubscriptionId,
            List<MaeilMailTrack> tracks
    ) {
        return tracks.stream()
                .map(track -> MaeilMailSubscriptionTrack.builder()
                        .maeilMailSubscriptionId(maeilMailSubscriptionId)
                        .field(track)
                        .build())
                .toList();
    }
}
