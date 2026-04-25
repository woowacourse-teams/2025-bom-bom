package me.bombom.api.v1.nativenewsletter.maeilmail.repository;

import static me.bombom.api.v1.nativenewsletter.maeilmail.domain.QMaeilMailSentContent.maeilMailSentContent;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSentContent;

@RequiredArgsConstructor
public class MaeilMailSentContentRepositoryImpl implements CustomMaeilMailSentContentRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<MaeilMailSentContent> findAllByMemberTopicKeys(Collection<MemberTopicKey> keys) {
        if (keys.isEmpty()) {
            return List.of();
        }

        return jpaQueryFactory
                .selectFrom(maeilMailSentContent)
                .where(memberTopicKeyCondition(keys))
                .fetch();
    }

    private BooleanBuilder memberTopicKeyCondition(Collection<MemberTopicKey> keys) {
        BooleanBuilder builder = new BooleanBuilder();
        keys.forEach(key -> builder.or(
                maeilMailSentContent.memberId.eq(key.memberId())
                        .and(maeilMailSentContent.topicId.eq(key.topicId()))
        ));
        return builder;
    }
}
