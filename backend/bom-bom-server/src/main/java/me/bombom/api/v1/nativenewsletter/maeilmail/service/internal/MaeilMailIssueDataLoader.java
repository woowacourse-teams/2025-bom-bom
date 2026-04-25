package me.bombom.api.v1.nativenewsletter.maeilmail.service.internal;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CServerErrorException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSentContent;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSubscriptionTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTopic;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.IssueData;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MemberTopicKey;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.TopicContentId;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailContentRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailIssueHistoryRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailSentContentRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailTopicRepository;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterSource;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MaeilMailIssueDataLoader {

    private final MaeilMailTopicRepository topicRepository;
    private final MaeilMailContentRepository contentRepository;
    private final MaeilMailSentContentRepository sentContentRepository;
    private final MaeilMailIssueHistoryRepository issueHistoryRepository;
    private final NewsletterRepository newsletterRepository;

    public IssueData load(LocalDate issueDate, List<MaeilMailSubscriptionTrack> tracks) {
        Long newsletterId = loadMaeilMailNewsletterId();
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

    private Long loadMaeilMailNewsletterId() {
        return newsletterRepository.findBySource(NewsletterSource.MAEIL_MAIL)
                .map(Newsletter::getId)
                .orElseThrow(() -> new CServerErrorException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "Newsletter")
                        .addContext("source", NewsletterSource.MAEIL_MAIL.name()));
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
