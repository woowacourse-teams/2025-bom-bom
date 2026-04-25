package news.bombomemail.nativenewsletter.maeilmail.repository;

import jakarta.persistence.EntityManager;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailSentContent;
import news.bombomemail.nativenewsletter.maeilmail.dto.MemberTopicKey;

@RequiredArgsConstructor
public class MaeilMailSentContentRepositoryImpl implements CustomMaeilMailSentContentRepository {

    private final EntityManager entityManager;

    @Override
    public List<MaeilMailSentContent> findAllByMemberTopicKeys(Collection<MemberTopicKey> keys) {
        if (keys.isEmpty()) {
            return List.of();
        }

        Set<MemberTopicKey> targetKeys = Set.copyOf(keys);
        Set<Long> memberIds = extractMemberIds(targetKeys);
        Set<Long> topicIds = extractTopicIds(targetKeys);
        return entityManager.createQuery("""
                        SELECT s FROM MaeilMailSentContent s
                        WHERE s.memberId IN :memberIds
                        AND s.topicId IN :topicIds
                        """, MaeilMailSentContent.class)
                .setParameter("memberIds", memberIds)
                .setParameter("topicIds", topicIds)
                .getResultList()
                .stream()
                .filter(sentContent -> targetKeys.contains(
                        new MemberTopicKey(sentContent.getMemberId(), sentContent.getTopicId())
                ))
                .toList();
    }

    private Set<Long> extractMemberIds(Collection<MemberTopicKey> keys) {
        return keys.stream()
                .map(MemberTopicKey::memberId)
                .collect(Collectors.toUnmodifiableSet());
    }

    private Set<Long> extractTopicIds(Collection<MemberTopicKey> keys) {
        return keys.stream()
                .map(MemberTopicKey::topicId)
                .collect(Collectors.toUnmodifiableSet());
    }
}
