package me.bombom.api.v1.highlight.repository;

import static me.bombom.api.v1.article.domain.QArticle.article;
import static me.bombom.api.v1.highlight.domain.QHighlight.highlight;
import static me.bombom.api.v1.newsletter.domain.QNewsletter.newsletter;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.highlight.dto.response.HighlightResponse;
import me.bombom.api.v1.highlight.dto.response.QHighlightLocationResponse;
import me.bombom.api.v1.highlight.dto.response.QHighlightResponse;

@RequiredArgsConstructor
public class HighlightRepositoryImpl implements CustomHighlightRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<HighlightResponse> findHighlights(Long memberId, Long articleId, Long newsletterId) {
        return jpaQueryFactory
                .select(new QHighlightResponse(
                        highlight.id,
                        new QHighlightLocationResponse(
                                highlight.highlightLocation.startOffset,
                                highlight.highlightLocation.startXPath,
                                highlight.highlightLocation.endOffset,
                                highlight.highlightLocation.endXPath
                        ),
                        highlight.articleId,
                        highlight.color.value,
                        highlight.text,
                        highlight.memo,
                        newsletter.name,
                        newsletter.imageUrl,
                        article.title,
                        highlight.createdAt
                ))
                .from(highlight)
                .join(article).on(article.id.eq(highlight.articleId))
                .join(newsletter).on(newsletter.id.eq(article.newsletterId))
                .where(createMemberIdWhereClause(memberId))
                .where(createArticleIdWhereClause(articleId))
                .where(createNewsletterIdWhereClause(newsletterId))
                .orderBy(highlight.createdAt.desc())
                .fetch();
    }

    private BooleanExpression createMemberIdWhereClause(Long memberId) {
        return article.memberId.eq(memberId);
    }

    private BooleanExpression createArticleIdWhereClause(Long articleId) {
        return Optional.ofNullable(articleId)
                .map(highlight.articleId::eq)
                .orElse(null);
    }

    private BooleanExpression createNewsletterIdWhereClause(Long newsletterId) {
        return Optional.ofNullable(newsletterId)
                .map(article.newsletterId::eq)
                .orElse(null);
    }
}
