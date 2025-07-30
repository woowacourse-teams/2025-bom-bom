package me.bombom.api.v1.article.repository;

import static me.bombom.api.v1.article.domain.QArticle.article;
import static me.bombom.api.v1.newsletter.domain.QCategory.category;
import static me.bombom.api.v1.newsletter.domain.QNewsletter.newsletter;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.dto.ArticleResponse;
import me.bombom.api.v1.article.dto.GetArticlesOptions;
import me.bombom.api.v1.article.dto.QArticleResponse;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.newsletter.dto.QNewsletterSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class ArticleRepositoryImpl implements CustomArticleRepository{

    private static final Map<String, Path<?>> SORT_FIELD_WHITELIST_MAP = Map.of(
            "title", article.title,
            "createdAt", article.createdAt,
            "arrivedDateTime", article.arrivedDateTime,
            "expectedReadTime", article.expectedReadTime
    );

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<ArticleResponse> findByMemberId(
            Long memberId,
            GetArticlesOptions options,
            Pageable pageable
    ) {
        Long total = getTotal(memberId, options);
        List<ArticleResponse> content = getContent(memberId, options, pageable);
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public int countAllByMemberId(Long memberId, String keyword) {
        return jpaQueryFactory.select(article.count())
                .from(article)
                .where(createMemberWhereClause(memberId))
                .where(createKeywordWhereClause(keyword))
                .fetchOne()
                .intValue();
    }

    @Override
    public int countAllByCategoryIdAndMemberId(Long memberId, Long categoryId, String keyword) {
        return jpaQueryFactory.select(article.count())
                .from(article)
                .join(newsletter).on(article.newsletterId.eq(newsletter.id))
                .join(category).on(newsletter.categoryId.eq(category.id))
                .where(createMemberWhereClause(memberId))
                .where(createKeywordWhereClause(keyword))
                .where(createCategoryIdWhereClause(categoryId))
                .fetchOne()
                .intValue();
    }

    private Long getTotal(Long memberId, GetArticlesOptions options) {
        return jpaQueryFactory.select(article.count())
                .from(article)
                .join(newsletter).on(article.newsletterId.eq(newsletter.id))
                .join(category).on(newsletter.categoryId.eq(category.id))
                .where(createMemberWhereClause(memberId))
                .where(createDateWhereClause(options.date()))
                .where(createKeywordWhereClause(options.keyword()))
                .fetchOne();
    }

    private List<ArticleResponse> getContent(Long memberId, GetArticlesOptions options, Pageable pageable) {
        return jpaQueryFactory.select(new QArticleResponse(
                        article.id,
                        article.title,
                        article.contentsSummary,
                        article.arrivedDateTime,
                        article.thumbnailUrl,
                        article.expectedReadTime,
                        article.isRead,
                        new QNewsletterSummaryResponse(newsletter.name, newsletter.imageUrl, category.name)
                ))
                .from(article)
                .join(newsletter).on(article.newsletterId.eq(newsletter.id))
                .join(category).on(newsletter.categoryId.eq(category.id))
                .where(createMemberWhereClause(memberId))
                .where(createDateWhereClause(options.date()))
                .where(createKeywordWhereClause(options.keyword()))
                .where(createCategoryNameWhereClause(options.category()))
                .orderBy(getOrderSpecifiers(pageable).toArray(OrderSpecifier[]::new))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private BooleanExpression createMemberWhereClause(Long memberId) {
        return article.memberId.eq(memberId);
    }

    private Predicate createCategoryNameWhereClause(String categoryName) {
        return categoryName != null ? category.name.eq(categoryName) : null;
    }

    private Predicate createCategoryIdWhereClause(Long categoryId) {
        return categoryId != null ? category.id.eq(categoryId) : null;
    }

    private Predicate createDateWhereClause(LocalDate date) {
        return date != null ? article.arrivedDateTime.between(
                date.atTime(LocalTime.MIN),
                date.atTime(LocalTime.MAX))
                : null;
    }

    private Predicate createKeywordWhereClause(String keyword) {
        return StringUtils.hasText(keyword) ? article.title.like("%" + keyword.trim() + "%") : null;
    }

    private List<OrderSpecifier<?>> getOrderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        pageable.getSort().stream().forEach(sort -> {
            Order order = sort.isAscending() ? Order.ASC : Order.DESC;
            String property = sort.getProperty();
            Path<?> target = resolveSortProperty(property);
            OrderSpecifier<?> orderSpecifier = new OrderSpecifier(order, target);
            orderSpecifiers.add(orderSpecifier);
        });
        return orderSpecifiers;
    }

    private static Path<?> resolveSortProperty(String property) {
        Path<?> sortPath = SORT_FIELD_WHITELIST_MAP.get(property);
        if (sortPath == null) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_REQUEST_PARAMETER_VALIDATION);
        }
        return sortPath;
    }
}
