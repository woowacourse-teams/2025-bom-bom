package me.bombom.api.v1.article.repository;

import static me.bombom.api.v1.article.domain.QArticle.article;
import static me.bombom.api.v1.bookmark.domain.QBookmark.bookmark;
import static me.bombom.api.v1.newsletter.domain.QCategory.category;
import static me.bombom.api.v1.newsletter.domain.QNewsletter.newsletter;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.article.dto.request.ArticleSearchOptionsRequest;
import me.bombom.api.v1.article.dto.request.ArticlesOptionsRequest;
import me.bombom.api.v1.article.dto.response.ArticleCountPerNewsletterResponse;
import me.bombom.api.v1.article.dto.response.ArticleResponse;
import me.bombom.api.v1.article.dto.response.QArticleCountPerNewsletterResponse;
import me.bombom.api.v1.article.dto.response.QArticleResponse;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.newsletter.dto.QNewsletterSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

@Slf4j
@RequiredArgsConstructor
public class ArticleRepositoryImpl implements CustomArticleRepository {

    private static final char LIKE_ESCAPE_CHARACTER = '!';

    private static final Map<String, Path<?>> SORT_FIELD_WHITELIST_MAP = Map.of(
            "title", article.title,
            "createdAt", article.createdAt,
            "arrivedDateTime", article.arrivedDateTime,
            "expectedReadTime", article.expectedReadTime
    );

    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;

    @Override
    public Page<ArticleResponse> findArticles(
            Long memberId,
            ArticlesOptionsRequest options,
            Pageable pageable
    ) {
        JPAQuery<Long> totalQuery = getTotalQuery(memberId, options);
        List<ArticleResponse> content = getContent(memberId, options, pageable);
        return PageableExecutionUtils.getPage(content, pageable, totalQuery::fetchOne);
    }

    @Override
    public Page<ArticleResponse> findArticlesBySearch(
            Long memberId,
            ArticleSearchOptionsRequest options,
            Pageable pageable
    ) {
        JPAQuery<Long> totalQuery = getTotalQueryForSearch(memberId, options);
        List<ArticleResponse> content = findArticlesFromArticleOnly(memberId, options, pageable);
        return PageableExecutionUtils.getPage(content, pageable, totalQuery::fetchOne);
    }

    @Override
    public List<ArticleCountPerNewsletterResponse> countPerNewsletter(Long memberId, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return countWithoutKeyword(memberId);
        }
        return countWithKeyword(memberId, keyword);
    }

    private List<ArticleCountPerNewsletterResponse> countWithoutKeyword(Long memberId) {
        return jpaQueryFactory
                .select(new QArticleCountPerNewsletterResponse(
                        newsletter.id,
                        newsletter.name,
                        newsletter.imageUrl.coalesce(""),
                        article.id.count().intValue()
                ))
                .from(article)
                .join(newsletter).on(article.newsletterId.eq(newsletter.id))
                .where(article.memberId.eq(memberId))
                .groupBy(newsletter.id)
                .orderBy(article.id.count().desc())
                .fetch();
    }

    private List<ArticleCountPerNewsletterResponse> countWithKeyword(Long memberId, String keyword) {
        return jpaQueryFactory
                .select(new QArticleCountPerNewsletterResponse(
                        newsletter.id,
                        newsletter.name,
                        newsletter.imageUrl.coalesce(""),
                        article.id.count().intValue()
                ))
                .from(article)
                .join(newsletter).on(article.newsletterId.eq(newsletter.id))
                .where(createMemberWhereClause(memberId))
                .where(createKeywordWhereClause(keyword))
                .groupBy(newsletter.id)
                .orderBy(article.id.count().desc())
                .fetch();
    }

    /**
     * article 테이블만 사용하여 조회
     */
    private List<ArticleResponse> findArticlesFromArticleOnly(
            Long memberId,
            ArticleSearchOptionsRequest options,
            Pageable pageable
    ) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        // 정렬 조건
        String orderBy = buildOrderByClause(pageable);

        // 페이징
        int pageSize = pageable.getPageSize();
        int offset = (int) pageable.getOffset();

        sql.append("SELECT ")
                .append("a.id as article_id, ")
                .append("a.title, ")
                .append("a.contents_summary, ")
                .append("a.arrived_date_time, ")
                .append("a.thumbnail_url, ")
                .append("a.expected_read_time, ")
                .append("a.is_read, ")
                .append("CASE WHEN EXISTS(SELECT 1 FROM bookmark b WHERE b.article_id = a.id AND b.member_id = ?) THEN 1 ELSE 0 END as is_bookmarked, ")
                .append("n.name as newsletter_name, ")
                .append("COALESCE(n.image_url, '') as newsletter_image_url, ")
                .append("c.name as category_name ")
                .append("FROM article a ")
                .append("INNER JOIN newsletter n ON n.id = a.newsletter_id ")
                .append("INNER JOIN category c ON c.id = n.category_id ")
                .append("WHERE a.member_id = ? ");
        params.add(memberId); // bookmark 서브쿼리용
        params.add(memberId); // member_id

        if (StringUtils.hasText(options.keyword())) {
            String keywordPattern = createKeywordPattern(options.keyword());
            sql.append("AND (LOWER(a.title) LIKE ? ESCAPE '!' OR LOWER(a.contents_text) LIKE ? ESCAPE '!') ");
            params.add(keywordPattern);
            params.add(keywordPattern);
        }

        if (options.newsletterId() != null) {
            sql.append("AND a.newsletter_id = ? ");
            params.add(options.newsletterId());
        }

        sql.append("ORDER BY ").append(orderBy).append(" ")
                .append("LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add(offset);

        // Native Query 실행
        try {
            Query nativeQuery = entityManager.createNativeQuery(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                nativeQuery.setParameter(i + 1, params.get(i));
            }

            @SuppressWarnings("unchecked")
            List<Object[]> results = nativeQuery.getResultList();

            // 결과를 ArticleResponse로 매핑
            return results.stream()
                    .map(row -> mapToArticleResponse(row))
                    .toList();
        } catch (Exception e) {
            log.error("SQL 쿼리 실행 실패 (article 테이블만) - SQL: {}, Params: {}", sql.toString(), params, e);
            throw e;
        }
    }

    private ArticleResponse mapToArticleResponse(Object[] row) {
        // MySQL의 EXISTS는 TINYINT(1)로 반환되므로 Number로 처리
        boolean isRead = row[6] instanceof Number ? ((Number) row[6]).intValue() == 1 : (Boolean) row[6];
        boolean isBookmarked = row[7] instanceof Number ? ((Number) row[7]).intValue() == 1 : (Boolean) row[7];

        // expected_read_time은 NULL일 수 있음
        Integer expectedReadTime = row[5] != null && row[5] instanceof Number
                ? ((Number) row[5]).intValue()
                : null;

        return new ArticleResponse(
                ((Number) row[0]).longValue(), // article_id
                (String) row[1], // title
                (String) row[2], // contents_summary
                ((java.sql.Timestamp) row[3]).toLocalDateTime(), // arrived_date_time
                (String) row[4], // thumbnail_url
                expectedReadTime, // expected_read_time
                isRead,
                isBookmarked,
                new me.bombom.api.v1.newsletter.dto.NewsletterSummaryResponse(
                        (String) row[8], // newsletter_name
                        (String) row[9], // newsletter_image_url
                        (String) row[10] // category_name
                )
        );
    }

    private String buildOrderByClause(Pageable pageable) {
        if (!pageable.getSort().isSorted()) {
            return "arrived_date_time DESC";
        }

        StringBuilder orderBy = new StringBuilder();
        boolean first = true;

        for (org.springframework.data.domain.Sort.Order order : pageable.getSort()) {
            if (!first) {
                orderBy.append(", ");
            }

            String property = order.getProperty();
            if (!StringUtils.hasText(property)) {
                throw new CIllegalArgumentException(ErrorDetail.INVALID_REQUEST_PARAMETER_VALIDATION)
                        .addContext("message", "정렬 필드가 비어있습니다.");
            }

            String column = switch (property.strip()) {
                case "arrivedDateTime" -> "arrived_date_time";
                case "title" -> "title";
                case "expectedReadTime" -> "expected_read_time";
                case "createdAt" -> "arrived_date_time"; // createdAt은 arrivedDateTime으로 대체
                default -> {
                    log.debug("허용되지 않는 정렬 키: {}", property);
                    throw new CIllegalArgumentException(ErrorDetail.INVALID_REQUEST_PARAMETER_VALIDATION)
                            .addContext("message", "허용되지 않는 정렬 필드입니다: " + property);
                }
            };

            orderBy.append(column);
            if (order.isDescending()) {
                orderBy.append(" DESC");
            } else {
                orderBy.append(" ASC");
            }

            first = false;
        }

        return orderBy.toString();
    }

    @Override
    public int countByMemberIdAndArrivedDateTimeAndIsRead(Long memberId, LocalDate date, boolean isRead) {
        BooleanExpression dateFilter = article.arrivedDateTime.between(
                date.atTime(LocalTime.MIN),
                date.atTime(LocalTime.MAX)
        );

        Long count = jpaQueryFactory.select(article.count())
                .from(article)
                .where(createMemberWhereClause(memberId))
                .where(dateFilter)
                .where(article.isRead.eq(isRead))
                .fetchOne();

        return Optional.ofNullable(count)
                .orElse(0L)
                .intValue();
    }

    private JPAQuery<Long> getTotalQuery(Long memberId, ArticlesOptionsRequest options) {
        if (options.unreadOnly()) {
            return jpaQueryFactory.select(article.count())
                    .from(article)
                    .join(newsletter).on(article.newsletterId.eq(newsletter.id))
                    .join(category).on(newsletter.categoryId.eq(category.id))
                    .where(createMemberWhereClause(memberId))
                    .where(createDateWhereClause(options.date()))
                    .where(createNewsletterIdWhereClause(options.newsletterId()))
                    .where(article.isRead.isFalse());
        }
        return jpaQueryFactory.select(article.count())
                .from(article)
                .join(newsletter).on(article.newsletterId.eq(newsletter.id))
                .join(category).on(newsletter.categoryId.eq(category.id))
                .where(createMemberWhereClause(memberId))
                .where(createDateWhereClause(options.date()))
                .where(createNewsletterIdWhereClause(options.newsletterId()));
    }

    private List<ArticleResponse> getContent(Long memberId, ArticlesOptionsRequest options, Pageable pageable) {
        if (options.unreadOnly()) {
            return jpaQueryFactory.select(new QArticleResponse(
                            article.id,
                            article.title,
                            article.contentsSummary,
                            article.arrivedDateTime,
                            article.thumbnailUrl,
                            article.expectedReadTime,
                            article.isRead,
                            getIsBookmarked(memberId),
                            new QNewsletterSummaryResponse(newsletter.name, newsletter.imageUrl, category.name)
                    ))
                    .from(article)
                    .join(newsletter).on(article.newsletterId.eq(newsletter.id))
                    .join(category).on(newsletter.categoryId.eq(category.id))
                    .where(createMemberWhereClause(memberId))
                    .where(createDateWhereClause(options.date()))
                    .where(createNewsletterIdWhereClause(options.newsletterId()))
                    .where(article.isRead.isFalse())
                    .orderBy(getOrderSpecifiers(pageable).toArray(OrderSpecifier[]::new))
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetch();
        }

        return jpaQueryFactory.select(new QArticleResponse(
                        article.id,
                        article.title,
                        article.contentsSummary,
                        article.arrivedDateTime,
                        article.thumbnailUrl,
                        article.expectedReadTime,
                        article.isRead,
                        getIsBookmarked(memberId),
                        new QNewsletterSummaryResponse(newsletter.name, newsletter.imageUrl, category.name)
                ))
                .from(article)
                .join(newsletter).on(article.newsletterId.eq(newsletter.id))
                .join(category).on(newsletter.categoryId.eq(category.id))
                .where(createMemberWhereClause(memberId))
                .where(createDateWhereClause(options.date()))
                .where(createNewsletterIdWhereClause(options.newsletterId()))
                .orderBy(getOrderSpecifiers(pageable).toArray(OrderSpecifier[]::new))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
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

    private BooleanExpression getIsBookmarked(Long memberId) {
        return JPAExpressions.selectOne()
                .from(bookmark)
                .where(
                        bookmark.articleId.eq(article.id)
                                .and(bookmark.memberId.eq(memberId))
                )
                .exists();
    }

    private JPAQuery<Long> getTotalQueryForSearch(
            Long memberId,
            ArticleSearchOptionsRequest options
    ) {
        return jpaQueryFactory.select(article.count())
                .from(article)
                .join(newsletter).on(article.newsletterId.eq(newsletter.id))
                .join(category).on(newsletter.categoryId.eq(category.id))
                .where(createMemberWhereClause(memberId))
                .where(createKeywordWhereClause(options.keyword()))
                .where(createNewsletterIdWhereClause(options.newsletterId()));
    }

    private BooleanExpression createMemberWhereClause(Long memberId) {
        return article.memberId.eq(memberId);
    }

    private BooleanExpression createKeywordWhereClause(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        String keywordPattern = createKeywordPattern(keyword);
        return article.title.lower().like(keywordPattern, LIKE_ESCAPE_CHARACTER)
                .or(article.contentsText.lower().like(keywordPattern, LIKE_ESCAPE_CHARACTER));
    }

    private String createKeywordPattern(String keyword) {
        String escapedKeyword = keyword.strip()
                .toLowerCase()
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
        return "%" + escapedKeyword + "%";
    }

    private BooleanExpression createDateWhereClause(LocalDate date) {
        if (date == null) {
            return null;
        }
        return article.arrivedDateTime.between(
                date.atStartOfDay(),
                date.atTime(23, 59, 59, 999_999_999)
        );
    }

    private BooleanExpression createNewsletterIdWhereClause(Long newsletterId) {
        return Optional.ofNullable(newsletterId)
                .map(newsletter.id::eq)
                .orElse(null);
    }
}
