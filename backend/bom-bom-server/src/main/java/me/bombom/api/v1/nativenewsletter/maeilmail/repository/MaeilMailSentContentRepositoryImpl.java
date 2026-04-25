package me.bombom.api.v1.nativenewsletter.maeilmail.repository;

import static me.bombom.api.v1.nativenewsletter.maeilmail.domain.QMaeilMailSentContent.maeilMailSentContent;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSentContent;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MemberTopicKey;

@RequiredArgsConstructor
public class MaeilMailSentContentRepositoryImpl implements CustomMaeilMailSentContentRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<MaeilMailSentContent> findAllByMemberTopicKeys(Collection<MemberTopicKey> keys) {
        if (keys.isEmpty()) {
            return List.of();
        }

        Set<MemberTopicKey> targetKeys = Set.copyOf(keys);
        Set<Long> memberIds = extractMemberIds(targetKeys);
        Set<Long> topicIds = extractTopicIds(targetKeys);
        return jpaQueryFactory
                .selectFrom(maeilMailSentContent)
                .where(
                        maeilMailSentContent.memberId.in(memberIds),
                        maeilMailSentContent.topicId.in(topicIds)
                )
                .fetch()
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
