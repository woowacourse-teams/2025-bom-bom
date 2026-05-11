package me.bombom.api.v1.nativenewsletter.maeilmail.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSubscriptionTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailSubscriptionResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailUpdateSubscriptionRequest;
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
    public void putSubscription(Member member, MaeilMailUpdateSubscriptionRequest request) {
        List<MaeilMailTrack> requestedTracks = request.tracks();
        validateTracks(requestedTracks);

        Long memberId = member.getId();
        Newsletter newsletter = getMaeilMailNewsletter();
        Optional<Subscribe> existing = subscribeRepository.findByMemberIdAndNewsletterId(memberId, newsletter.getId());

        if (existing.isEmpty() && requestedTracks.isEmpty()) {
            return;
        }

        if (requestedTracks.isEmpty()) {
            removeSubscription(existing.get(), memberId);
            return;
        }

        Subscribe subscribe = existing.orElseGet(() -> createSubscribe(member, newsletter));
        replaceTracks(subscribe, memberId, requestedTracks);
    }

    private Subscribe createSubscribe(Member member, Newsletter newsletter) {
        Subscribe subscribe = subscribeRepository.save(Subscribe.builder()
                .memberId(member.getId())
                .newsletterId(newsletter.getId())
                .build());
        applicationEventPublisher.publishEvent(MaeilMailSubscribedEvent.of(newsletter.getId(), member.getBirthDate()));
        return subscribe;
    }

    private void removeSubscription(Subscribe subscribe, Long memberId) {
        maeilMailSubscriptionTrackRepository.deleteByMemberId(memberId);
        subscribeRepository.delete(subscribe);
    }

    private void replaceTracks(Subscribe subscribe, Long memberId, List<MaeilMailTrack> requestedTracks) {
        List<MaeilMailSubscriptionTrack> currentTracks = maeilMailSubscriptionTrackRepository.findByMemberId(memberId);
        Set<MaeilMailTrack> requestedTrackSet = new LinkedHashSet<>(requestedTracks);
        Set<MaeilMailTrack> currentTrackSet = currentTracks.stream()
                .map(MaeilMailSubscriptionTrack::getField)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<MaeilMailSubscriptionTrack> tracksToRemove = currentTracks.stream()
                .filter(track -> !requestedTrackSet.contains(track.getField()))
                .toList();

        List<MaeilMailTrack> tracksToAdd = requestedTrackSet.stream()
                .filter(track -> !currentTrackSet.contains(track))
                .toList();

        maeilMailSubscriptionTrackRepository.deleteAll(tracksToRemove);
        maeilMailSubscriptionTrackRepository.saveAll(buildSubscriptionTracks(subscribe.getId(), memberId, tracksToAdd));
    }

    private void validateTracks(List<MaeilMailTrack> tracks) {
        long distinctTrackCount = tracks.stream()
                .distinct()
                .count();

        if (distinctTrackCount != tracks.size()) {
            throw new CIllegalArgumentException(ErrorDetail.DUPLICATED_DATA)
                    .addContext(ErrorContextKeys.OPERATION, "validateTracks")
                    .addContext(ErrorContextKeys.DETAIL, "중복된 트랙은 선택할 수 없습니다.");
        }
    }

    private Newsletter getMaeilMailNewsletter() {
        return newsletterRepository.findBySource(NewsletterSource.MAEIL_MAIL)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "newsletter")
                        .addContext(ErrorContextKeys.DETAIL, "매일메일 뉴스레터가 존재하지 않습니다."));
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
