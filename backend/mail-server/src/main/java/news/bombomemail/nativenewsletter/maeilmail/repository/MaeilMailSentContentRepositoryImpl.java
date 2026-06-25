package news.bombomemail.nativenewsletter.maeilmail.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailSentContent;
import news.bombomemail.nativenewsletter.maeilmail.domain.QMaeilMailSentContent;
import news.bombomemail.nativenewsletter.maeilmail.dto.MemberTopicKey;

@RequiredArgsConstructor
public class MaeilMailSentContentRepositoryImpl implements CustomMaeilMailSentContentRepository {

    private final EntityManager entityManager;

    @Override
    public List<MaeilMailSentContent> findAllByMemberTopicKeys(Collection<MemberTopicKey> keys) {
        if (keys.isEmpty()) {
            return List.of();
        }

        QMaeilMailSentContent sentContent = QMaeilMailSentContent.maeilMailSentContent;
        return new JPAQueryFactory(entityManager)
                .selectFrom(sentContent)
                .where(memberTopicCondition(sentContent, keys))
                .fetch();
    }

    private BooleanBuilder memberTopicCondition(
            QMaeilMailSentContent sentContent,
            Collection<MemberTopicKey> keys
    ) {
        BooleanBuilder condition = new BooleanBuilder();
        keys.stream()
                .distinct()
                .forEach(key -> condition.or(
                        sentContent.memberId.eq(key.memberId())
                                .and(sentContent.topicId.eq(key.topicId()))
                ));
        return condition;
    }
}
