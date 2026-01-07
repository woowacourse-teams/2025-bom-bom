package me.bombom.api.v1.highlight.repository;

import static me.bombom.api.v1.article.domain.QArticle.article;
import static me.bombom.api.v1.highlight.domain.QHighlight.highlight;
import static me.bombom.api.v1.newsletter.domain.QNewsletter.newsletter;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.dto.response.ChallengeCommentHighlightResponse;
import me.bombom.api.v1.challenge.dto.response.QChallengeCommentHighlightResponse;
import me.bombom.api.v1.highlight.dto.response.HighlightCountPerNewsletterResponse;
import me.bombom.api.v1.highlight.dto.response.HighlightResponse;
import me.bombom.api.v1.highlight.dto.response.QHighlightCountPerNewsletterResponse;
import me.bombom.api.v1.highlight.dto.response.QHighlightLocationResponse;
import me.bombom.api.v1.highlight.dto.response.QHighlightResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class HighlightRepositoryImpl implements CustomHighlightRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<HighlightResponse> findHighlights(
            Long memberId,
            Long articleId,
            Long newsletterId,
            Pageable pageable
    ) {
        JPAQuery<Long> totalQuery = getTotalQuery(memberId, articleId, newsletterId);
        List<HighlightResponse> content = getContent(memberId, articleId, newsletterId, pageable);
        return PageableExecutionUtils.getPage(content, pageable, totalQuery::fetchOne);
    }

    @Override
    public Page<ChallengeCommentHighlightResponse> findChallengeArticleHighlights(
            Long memberId,
            Long articleId,
            double textTruncateRatio,
            Pageable pageable
    ) {
        JPAQuery<Long> totalQuery = getTotalQuery(memberId, articleId, null);

        List<ChallengeCommentHighlightResponse> content = jpaQueryFactory
                .select(new QChallengeCommentHighlightResponse(
                        highlight.id,
                        truncatedHighlightText(textTruncateRatio),
                        highlight.memo
                ))
                .from(highlight)
                .join(article).on(article.id.eq(highlight.articleId))
                .where(createMemberIdWhereClause(memberId))
                .where(createArticleIdWhereClause(articleId))
                .orderBy(highlight.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return PageableExecutionUtils.getPage(content, pageable, totalQuery::fetchOne);
    }

    @Override
    public List<HighlightCountPerNewsletterResponse> countPerNewsletters(Long memberId) {
        return jpaQueryFactory
                .select(new QHighlightCountPerNewsletterResponse(
                        newsletter.id,
                        newsletter.name,
                        newsletter.imageUrl,
                        highlight.id.count()
                ))
                .from(highlight)
                .join(newsletter).on(newsletter.id.eq(highlight.newsletterId))
                .where(createMemberIdWhereClause(memberId))
                .groupBy(newsletter.id, newsletter.name, newsletter.imageUrl)
                .having(highlight.id.count().gt(0))
                .fetch();
    }

    private List<HighlightResponse> getContent(Long memberId, Long articleId, Long newsletterId, Pageable pageable) {
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
                        highlight.title,
                        highlight.createdAt
                ))
                .from(highlight)
                .join(newsletter).on(newsletter.id.eq(highlight.newsletterId))
                .where(createMemberIdWhereClause(memberId))
                .where(createArticleIdWhereClause(articleId))
                .where(createNewsletterIdWhereClause(newsletterId))
                .orderBy(highlight.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private JPAQuery<Long> getTotalQuery(Long memberId, Long articleId, Long newsletterId) {
        return jpaQueryFactory.select(highlight.count())
                .from(highlight)
                .where(createMemberIdWhereClause(memberId))
                .where(createArticleIdWhereClause(articleId))
                .where(createNewsletterIdWhereClause(newsletterId));
    }

    private BooleanExpression createMemberIdWhereClause(Long memberId) {
        return highlight.memberId.eq(memberId);
    }

    private BooleanExpression createArticleIdWhereClause(Long articleId) {
        return Optional.ofNullable(articleId)
                .map(highlight.articleId::eq)
                .orElse(null);
    }

    private BooleanExpression createNewsletterIdWhereClause(Long newsletterId) {
        return Optional.ofNullable(newsletterId)
                .map(highlight.newsletterId::eq)
                .orElse(null);
    }

    private Expression<String> truncatedHighlightText(double textTruncateRatio) {
        NumberExpression<Integer> threshold =
                article.contentsText.length()
                        .doubleValue()
                        .multiply(textTruncateRatio)
                        .ceil()
                        .intValue();

        return new CaseBuilder()
                .when(highlight.text.length().gt(threshold))
                .then(highlight.text.substring(0, threshold).concat("..."))
                .otherwise(highlight.text);
    }
}
