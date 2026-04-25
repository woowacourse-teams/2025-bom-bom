package me.bombom.api.v1.nativenewsletter.maeilmail.repository;

import static me.bombom.api.v1.nativenewsletter.maeilmail.domain.QMaeilMailIssueHistory.maeilMailIssueHistory;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MemberTopicKey;

@RequiredArgsConstructor
public class MaeilMailIssueHistoryRepositoryImpl implements CustomMaeilMailIssueHistoryRepository {

    private final JPAQueryFactory jpaQueryFactory;

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
        return jpaQueryFactory
                .selectFrom(maeilMailIssueHistory)
                .where(
                        maeilMailIssueHistory.issueDate.eq(issueDate),
                        maeilMailIssueHistory.memberId.in(memberIds),
                        maeilMailIssueHistory.topicId.in(topicIds)
                )
                .fetch()
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
