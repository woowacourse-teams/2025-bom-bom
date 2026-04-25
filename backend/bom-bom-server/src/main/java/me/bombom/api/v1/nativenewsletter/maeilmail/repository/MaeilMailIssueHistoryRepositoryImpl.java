package me.bombom.api.v1.nativenewsletter.maeilmail.repository;

import static me.bombom.api.v1.nativenewsletter.maeilmail.domain.QMaeilMailIssueHistory.maeilMailIssueHistory;

import com.querydsl.core.BooleanBuilder;
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
        return jpaQueryFactory
                .selectFrom(maeilMailIssueHistory)
                .where(
                        maeilMailIssueHistory.issueDate.eq(issueDate),
                        memberTopicKeyCondition(targetKeys)
                )
                .fetch()
                .stream()
                .map(history -> new MemberTopicKey(history.getMemberId(), history.getTopicId()))
                .collect(Collectors.toUnmodifiableSet());
    }

    private BooleanBuilder memberTopicKeyCondition(Collection<MemberTopicKey> keys) {
        BooleanBuilder builder = new BooleanBuilder();
        keys.forEach(key -> builder.or(
                maeilMailIssueHistory.memberId.eq(key.memberId())
                        .and(maeilMailIssueHistory.topicId.eq(key.topicId()))
        ));
        return builder;
    }
}
