package me.bombom.api.v1.article.repository;

import static me.bombom.api.v1.article.domain.QArticle.article;
import static me.bombom.api.v1.newsletter.domain.QCategory.category;
import static me.bombom.api.v1.newsletter.domain.QNewsletter.newsletter;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.dto.ArticleResponse;
import me.bombom.api.v1.article.dto.GetArticlesOptions;
import me.bombom.api.v1.article.dto.QArticleResponse;
import me.bombom.api.v1.newsletter.dto.QNewsletterSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class ArticleRepositoryImpl implements CustomArticleRepository{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<ArticleResponse> findByMemberId(
            Long memberId,
            GetArticlesOptions options,
            Pageable pageable
    ) {
        Long total = jpaQueryFactory.select(article.count())
                .from(article)
                .join(newsletter).on(article.newsletterId.eq(newsletter.id))
                .join(category).on(newsletter.categoryId.eq(category.id))
                .where(createMemberWhereClause(memberId))
                .where(createDateWhereClause(options.date()))
                .where(createKeywordWhereClause(options.keyword()))
                .fetchOne();

        List<OrderSpecifier<?>> orderSpecifiers = getOrderSpecifiers(pageable);

        List<ArticleResponse> content = jpaQueryFactory.select(new QArticleResponse(
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
                .orderBy(orderSpecifiers.stream().toArray(OrderSpecifier[]::new))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public int countAllByMemberId(Long memberId, String keyword) {
        return jpaQueryFactory.select(article.count())
                .from(article)
                .where(createMemberWhereClause(memberId))
                .where(StringUtils.hasText(keyword) ? article.title.like("%" + keyword + "%") : null)
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
                .where(StringUtils.hasText(keyword) ? article.title.like("%" + keyword.trim() + "%") : null)
                .where(createCategoryIdWhereClause(categoryId))
                .fetchOne()
                .intValue();
    }

    private Predicate createCategoryNameWhereClause(String categoryName) {
        return categoryName != null ? category.name.eq(categoryName) : null;
    }

    private Predicate createCategoryIdWhereClause(Long categoryId) {
        return categoryId != null ? category.id.eq(categoryId) : null;
    }

    private BooleanExpression createMemberWhereClause(Long memberId) {
        return article.memberId.eq(memberId);
    }

    private Predicate createDateWhereClause(LocalDate date) {
        return date != null ? article.arrivedDateTime.between(
                date.atTime(LocalTime.MIN),
                date.atTime(LocalTime.MAX))
                : null;
    }

    private Predicate createKeywordWhereClause(String keyword) {
        return StringUtils.hasText(keyword) ? article.title.like("%" + keyword + "%") : null;
    }

    private List<OrderSpecifier<?>> getOrderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        pageable.getSort().stream().forEach(sort -> {
            Order order = sort.isAscending() ? Order.ASC : Order.DESC;
            String property = sort.getProperty();
            Path<Object> target = Expressions.path(Object.class, article, property);
            OrderSpecifier<?> orderSpecifier = new OrderSpecifier(order, target);
            orderSpecifiers.add(orderSpecifier);
        });
        return orderSpecifiers;
    }
}
