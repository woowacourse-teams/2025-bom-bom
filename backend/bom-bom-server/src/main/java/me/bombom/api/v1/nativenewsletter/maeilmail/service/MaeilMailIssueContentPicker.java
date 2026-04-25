package me.bombom.api.v1.nativenewsletter.maeilmail.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailSentContentRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MemberTopicKey;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MaeilMailIssueContentPicker {

    private final MaeilMailSentContentRepository sentContentRepository;
    private final Random random = new Random();

    public Optional<Long> pick(
            Long memberId,
            Long topicId,
            Map<Long, List<Long>> contentIdsByTopicId,
            Map<MemberTopicKey, List<Long>> sentContentIdsByMemberTopic
    ) {
        List<Long> contentIds = contentIdsByTopicId.getOrDefault(topicId, List.of());
        if (contentIds.isEmpty()) {
            return Optional.empty();
        }

        MemberTopicKey topicKey = new MemberTopicKey(memberId, topicId);
        Set<Long> sentContentIds = new HashSet<>(sentContentIdsByMemberTopic.getOrDefault(topicKey, List.of()));
        List<Long> availableContentIds = contentIds.stream()
                .filter(contentId -> !sentContentIds.contains(contentId))
                .toList();
        if (!availableContentIds.isEmpty()) {
            return Optional.of(pickRandom(availableContentIds));
        }

        sentContentRepository.deleteByMemberIdAndTopicId(memberId, topicId);
        sentContentRepository.flush();
        return Optional.of(pickRandom(contentIds));
    }

    private Long pickRandom(List<Long> candidates) {
        return candidates.get(random.nextInt(candidates.size()));
    }
}
