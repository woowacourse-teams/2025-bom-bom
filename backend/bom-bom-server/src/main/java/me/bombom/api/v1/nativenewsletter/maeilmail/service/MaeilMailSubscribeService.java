package me.bombom.api.v1.nativenewsletter.maeilmail.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSubscriptionTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailSubscribeRequest;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailSubscriptionResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.event.MaeilMailSubscribedEvent;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailSubscriptionTrackRepository;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterSource;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.api.v1.subscribe.domain.Subscribe;
import me.bombom.api.v1.subscribe.repository.SubscribeRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaeilMailSubscribeService {

    private final SubscribeRepository subscribeRepository;
    private final NewsletterRepository newsletterRepository;
    private final MaeilMailSubscriptionTrackRepository maeilMailSubscriptionTrackRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public MaeilMailSubscriptionResponse getSubscription(Long memberId) {
        List<MaeilMailSubscriptionTrack> tracks = maeilMailSubscriptionTrackRepository.findByMemberId(memberId);
        return MaeilMailSubscriptionResponse.from(tracks);
    }

    @Transactional
    public void subscribe(Member member, MaeilMailSubscribeRequest request) {
        Newsletter newsletter = getMaeilMailNewsletter();
        validateNotSubscribed(member.getId(), newsletter.getId());
        validateTracks(request);

        Subscribe subscribe = subscribeRepository.save(Subscribe.builder()
                .memberId(member.getId())
                .newsletterId(newsletter.getId())
                .build());

        maeilMailSubscriptionTrackRepository.saveAll(buildSubscriptionTracks(subscribe.getId(), member.getId(), request.tracks()));
        applicationEventPublisher.publishEvent(MaeilMailSubscribedEvent.of(newsletter.getId(), member.getBirthDate()));
    }

    private Newsletter getMaeilMailNewsletter() {
        return newsletterRepository.findBySource(NewsletterSource.MAEIL_MAIL)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "newsletter")
                        .addContext(ErrorContextKeys.DETAIL, "매일메일 뉴스레터가 존재하지 않습니다."));
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
            Long subscribeId,
            Long memberId,
            List<MaeilMailTrack> tracks
    ) {
        return tracks.stream()
                .map(track -> MaeilMailSubscriptionTrack.builder()
                        .subscribeId(subscribeId)
                        .memberId(memberId)
                        .field(track)
                        .build())
                .toList();
    }
}
