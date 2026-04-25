package news.bombomemail.nativenewsletter.maeilmail.repository;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailIssueHistory;
import news.bombomemail.nativenewsletter.maeilmail.dto.MemberTopicKey;

@RequiredArgsConstructor
public class MaeilMailIssueHistoryRepositoryImpl implements CustomMaeilMailIssueHistoryRepository {

    private final EntityManager entityManager;

    @Override
    public Set<MemberTopicKey> findIssuedMemberTopicKeys(
            LocalDate issueDate,
            Collection<MemberTopicKey> keys
    ) {
        if (keys.isEmpty()) {
            return Set.of();
        }

        Set<MemberTopicKey> targetKeys = Set.copyOf(keys);
        Set<Long> memberIds = extractMemberIds(targetKeys);
        Set<Long> topicIds = extractTopicIds(targetKeys);
        return entityManager.createQuery("""
                        SELECT h FROM MaeilMailIssueHistory h
                        WHERE h.issueDate = :issueDate
                        AND h.memberId IN :memberIds
                        AND h.topicId IN :topicIds
                        """, MaeilMailIssueHistory.class)
                .setParameter("issueDate", issueDate)
                .setParameter("memberIds", memberIds)
                .setParameter("topicIds", topicIds)
                .getResultList()
                .stream()
                .map(history -> new MemberTopicKey(history.getMemberId(), history.getTopicId()))
                .filter(targetKeys::contains)
                .collect(Collectors.toUnmodifiableSet());
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
