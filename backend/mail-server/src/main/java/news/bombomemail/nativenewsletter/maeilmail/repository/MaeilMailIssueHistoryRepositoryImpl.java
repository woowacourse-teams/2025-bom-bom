package news.bombomemail.nativenewsletter.maeilmail.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import news.bombomemail.article.domain.QArticle;
import news.bombomemail.nativenewsletter.maeilmail.domain.QMaeilMailContent;
import news.bombomemail.nativenewsletter.maeilmail.domain.QMaeilMailIssueHistory;
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

        QMaeilMailIssueHistory issueHistory = QMaeilMailIssueHistory.maeilMailIssueHistory;
        QArticle article = QArticle.article;
        QMaeilMailContent content = QMaeilMailContent.maeilMailContent;
        LocalDateTime startOfDay = issueDate.atStartOfDay();
        LocalDateTime nextDay = issueDate.plusDays(1).atStartOfDay();
        return new JPAQueryFactory(entityManager)
                .select(Projections.constructor(
                        MemberTopicKey.class,
                        article.memberId,
                        content.topicId
                ))
                .from(issueHistory)
                .join(article).on(article.id.eq(issueHistory.articleId))
                .join(content).on(content.id.eq(issueHistory.contentId))
                .where(
                        article.arrivedDateTime.goe(startOfDay),
                        article.arrivedDateTime.lt(nextDay),
                        memberTopicCondition(article, content, keys)
                )
                .fetch()
                .stream()
                .collect(Collectors.toUnmodifiableSet());
    }

    private BooleanBuilder memberTopicCondition(
            QArticle article,
            QMaeilMailContent content,
            Collection<MemberTopicKey> keys
    ) {
        BooleanBuilder condition = new BooleanBuilder();
        keys.stream()
                .distinct()
                .forEach(key -> condition.or(
                        article.memberId.eq(key.memberId())
                                .and(content.topicId.eq(key.topicId()))
                ));
        return condition;
    }
}
