package me.bombom.api.v1.bookmark.repository;

import static me.bombom.api.v1.article.domain.QArticle.article;
import static me.bombom.api.v1.bookmark.domain.QBookmark.bookmark;
import static me.bombom.api.v1.newsletter.domain.QCategory.category;
import static me.bombom.api.v1.newsletter.domain.QNewsletter.newsletter;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.article.dto.QArticleResponse;
import me.bombom.api.v1.bookmark.dto.response.BookmarkResponse;
import me.bombom.api.v1.bookmark.dto.response.QBookmarkResponse;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.newsletter.dto.QNewsletterSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

@Slf4j
@RequiredArgsConstructor
public class BookmarkRepositoryImpl implements CustomBookmarkRepository {

    private static final Map<String, Path<?>> SORT_FIELD_WHITELIST_MAP = Map.of(
            "createdAt", bookmark.createdAt
    );

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<BookmarkResponse> findByMemberId(Long memberId, Pageable pageable) {
        JPAQuery<Long> totalQuery = getTotalQuery(memberId);
        List<BookmarkResponse> content = getContents(memberId, pageable);
        return PageableExecutionUtils.getPage(content, pageable, totalQuery::fetchOne);
    }

    @Override
    public int countAllByMemberId(Long memberId) {
        Long count = jpaQueryFactory.select(bookmark.count())
                .from(bookmark)
                .where(createBookmarkMemberWhereClause(memberId))
                .fetchOne();

        return Optional.ofNullable(count)
                .orElse(0L)
                .intValue();
    }

    @Override
    public int countAllByMemberIdAndNewsletterId(Long memberId, Long newsletterId) {
        Long count = jpaQueryFactory.select(bookmark.count())
                .from(bookmark)
                .join(article).on(bookmark.articleId.eq(article.id))
                .where(createBookmarkMemberWhereClause(memberId))
                .where(createNewsletterIdWhereClause(newsletterId))
                .fetchOne();

        return Optional.ofNullable(count)
                .orElse(0L)
                .intValue();
    }

    private JPAQuery<Long> getTotalQuery(Long memberId) {
        return jpaQueryFactory.select(bookmark.count())
                .from(bookmark)
                .where(createBookmarkMemberWhereClause(memberId));
    }

    private List<BookmarkResponse> getContents(Long memberId, Pageable pageable) {
        return jpaQueryFactory.select(new QBookmarkResponse(
                        bookmark.id,
                        new QArticleResponse(
                                article.id,
                                article.title,
                                article.contentsSummary,
                                article.arrivedDateTime,
                                article.thumbnailUrl,
                                article.expectedReadTime,
                                article.isRead,
                                new QNewsletterSummaryResponse(newsletter.name, newsletter.imageUrl, category.name)
                        )
                ))
                .from(bookmark)
                .join(article).on(bookmark.articleId.eq(article.id))
                .join(newsletter).on(article.newsletterId.eq(newsletter.id))
                .join(category).on(newsletter.categoryId.eq(category.id))
                .where(createBookmarkMemberWhereClause(memberId))
                .orderBy(getOrderSpecifiers(pageable).toArray(OrderSpecifier[]::new))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private BooleanExpression createBookmarkMemberWhereClause(Long memberId) {
        return bookmark.memberId.eq(memberId);
    }

    private BooleanExpression createNewsletterIdWhereClause(Long newsletterId) {
        return article.newsletterId.eq(newsletterId);
    }

    private List<OrderSpecifier<?>> getOrderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        pageable.getSort()
                .stream()
                .forEach(sort -> {
                    Order order = sort.isAscending() ? Order.ASC : Order.DESC;
                    String property = sort.getProperty();
                    Path<?> target = resolveSortProperty(property);
                    OrderSpecifier<?> orderSpecifier = new OrderSpecifier(order, target);
                    orderSpecifiers.add(orderSpecifier);
                });
        return orderSpecifiers;
    }

    private Path<?> resolveSortProperty(String property) {
        if (!StringUtils.hasText(property)) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_REQUEST_PARAMETER_VALIDATION);
        }

        String normalized = property.strip();
        return Optional.ofNullable(SORT_FIELD_WHITELIST_MAP.get(normalized))
                .orElseThrow(() -> {
                    log.debug("허용되지 않는 정렬 키: {}", property);
                    return new CIllegalArgumentException(ErrorDetail.INVALID_REQUEST_PARAMETER_VALIDATION);
                });
    }
}
