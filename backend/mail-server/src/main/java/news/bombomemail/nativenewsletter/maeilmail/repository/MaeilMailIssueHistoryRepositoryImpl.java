package news.bombomemail.nativenewsletter.maeilmail.repository;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
        LocalDateTime startOfDay = issueDate.atStartOfDay();
        LocalDateTime nextDay = issueDate.plusDays(1).atStartOfDay();
        return entityManager.createQuery("""
                        SELECT new news.bombomemail.nativenewsletter.maeilmail.dto.MemberTopicKey(
                            a.memberId,
                            c.topicId
                        )
                        FROM MaeilMailIssueHistory h
                        JOIN Article a ON a.id = h.articleId
                        JOIN MaeilMailContent c ON c.id = h.contentId
                        WHERE a.arrivedDateTime >= :startOfDay
                        AND a.arrivedDateTime < :nextDay
                        AND a.memberId IN :memberIds
                        AND c.topicId IN :topicIds
                        """, MemberTopicKey.class)
                .setParameter("startOfDay", startOfDay)
                .setParameter("nextDay", nextDay)
                .setParameter("memberIds", memberIds)
                .setParameter("topicIds", topicIds)
                .getResultList()
                .stream()
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
