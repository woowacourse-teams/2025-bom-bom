package news.bombomemail.nativenewsletter.maeilmail.service.internal;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailSentContent;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailSubscriptionTrack;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailTopic;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailTrack;
import news.bombomemail.nativenewsletter.maeilmail.dto.IssueData;
import news.bombomemail.nativenewsletter.maeilmail.dto.MemberTopicKey;
import news.bombomemail.nativenewsletter.maeilmail.dto.TopicContentId;
import news.bombomemail.nativenewsletter.maeilmail.repository.MaeilMailContentRepository;
import news.bombomemail.nativenewsletter.maeilmail.repository.MaeilMailIssueHistoryRepository;
import news.bombomemail.nativenewsletter.maeilmail.repository.MaeilMailSentContentRepository;
import news.bombomemail.nativenewsletter.maeilmail.repository.MaeilMailTopicRepository;
import news.bombomemail.subscribe.domain.Subscribe;
import news.bombomemail.subscribe.repository.SubscribeRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MaeilMailIssueDataLoader {

    private final MaeilMailTopicRepository topicRepository;
    private final MaeilMailContentRepository contentRepository;
    private final MaeilMailSentContentRepository sentContentRepository;
    private final MaeilMailIssueHistoryRepository issueHistoryRepository;
    private final SubscribeRepository subscribeRepository;

    public IssueData load(LocalDate issueDate, List<MaeilMailSubscriptionTrack> tracks) {
        Long newsletterId = loadNewsletterId(tracks);
        Map<MaeilMailTrack, List<MaeilMailTopic>> orderedTopicsByTrack = loadOrderedTopicsByTrack();
        Map<Long, MaeilMailTopic> issueTopicsByTrackId = resolveIssueTopicsByTrackId(tracks, orderedTopicsByTrack);
        Map<Long, List<Long>> contentIdsByTopicId = loadContentIdsByTopicId(issueTopicsByTrackId.values());
        List<MemberTopicKey> memberTopicKeys = resolveIssueMemberTopicKeys(tracks, issueTopicsByTrackId);
        Map<MemberTopicKey, List<Long>> sentContentIdsByMemberTopic = loadSentContentIdsByMemberTopic(memberTopicKeys);
        Set<MemberTopicKey> issuedMemberTopicKeys = loadIssuedMemberTopicKeys(issueDate, memberTopicKeys);

        return new IssueData(
                issueTopicsByTrackId,
                contentIdsByTopicId,
                sentContentIdsByMemberTopic,
                issuedMemberTopicKeys,
                newsletterId
        );
    }

    private Long loadNewsletterId(List<MaeilMailSubscriptionTrack> tracks) {
        List<Long> subscribeIds = tracks.stream()
                .map(MaeilMailSubscriptionTrack::getSubscribeId)
                .distinct()
                .toList();
        if (subscribeIds.isEmpty()) {
            throw new IllegalStateException("매일메일 발행 대상 Subscribe가 없습니다.");
        }

        List<Subscribe> subscribes = subscribeRepository.findAllById(subscribeIds);
        if (subscribes.size() != subscribeIds.size()) {
            throw new IllegalStateException("매일메일 발행 track의 Subscribe를 찾을 수 없습니다. subscribeIds=" + subscribeIds);
        }

        List<Long> newsletterIds = subscribes.stream()
                .map(Subscribe::getNewsletterId)
                .distinct()
                .toList();
        if (newsletterIds.size() != 1) {
            throw new IllegalStateException("발행 대상 track의 newsletterId가 하나가 아닙니다. newsletterIds=" + newsletterIds);
        }
        return newsletterIds.getFirst();
    }

    private Map<MaeilMailTrack, List<MaeilMailTopic>> loadOrderedTopicsByTrack() {
        return topicRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .collect(Collectors.groupingBy(MaeilMailTopic::getTrack));
    }

    private Map<Long, MaeilMailTopic> resolveIssueTopicsByTrackId(
            List<MaeilMailSubscriptionTrack> tracks,
            Map<MaeilMailTrack, List<MaeilMailTopic>> topicsByTrack
    ) {
        return tracks.stream()
                .map(track -> Map.entry(track, topicsByTrack.getOrDefault(track.getField(), List.of())))
                .filter(entry -> !entry.getValue().isEmpty())
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getId(),
                        entry -> pickIssueTopic(entry.getKey(), entry.getValue())
                ));
    }

    private MaeilMailTopic pickIssueTopic(MaeilMailSubscriptionTrack track, List<MaeilMailTopic> topics) {
        return topics.get(track.getCurriculumIndex() % topics.size());
    }

    private Map<Long, List<Long>> loadContentIdsByTopicId(Collection<MaeilMailTopic> topics) {
        List<Long> topicIds = extractTopicIds(topics);
        if (topicIds.isEmpty()) {
            return Map.of();
        }

        return contentRepository.findContentIdsByTopicIdIn(topicIds)
                .stream()
                .collect(Collectors.groupingBy(
                        TopicContentId::topicId,
                        Collectors.mapping(TopicContentId::contentId, Collectors.toList())
                ));
    }

    private Map<MemberTopicKey, List<Long>> loadSentContentIdsByMemberTopic(
            List<MemberTopicKey> memberTopicKeys
    ) {
        if (memberTopicKeys.isEmpty()) {
            return Map.of();
        }

        return sentContentRepository.findAllByMemberTopicKeys(memberTopicKeys).stream()
                .collect(Collectors.groupingBy(
                        sentContent -> new MemberTopicKey(sentContent.getMemberId(), sentContent.getTopicId()),
                        Collectors.mapping(MaeilMailSentContent::getContentId, Collectors.toList())
                ));
    }

    private Set<MemberTopicKey> loadIssuedMemberTopicKeys(
            LocalDate issueDate,
            List<MemberTopicKey> memberTopicKeys
    ) {
        return issueHistoryRepository.findIssuedMemberTopicKeys(issueDate, memberTopicKeys);
    }

    private List<MemberTopicKey> resolveIssueMemberTopicKeys(
            List<MaeilMailSubscriptionTrack> tracks,
            Map<Long, MaeilMailTopic> issueTopicsByTrackId
    ) {
        return tracks.stream()
                .map(track -> {
                    MaeilMailTopic topic = issueTopicsByTrackId.get(track.getId());
                    if (topic == null) {
                        return null;
                    }
                    return new MemberTopicKey(track.getMemberId(), topic.getId());
                })
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private List<Long> extractTopicIds(Collection<MaeilMailTopic> topics) {
        return topics.stream()
                .map(MaeilMailTopic::getId)
                .distinct()
                .toList();
    }
}
