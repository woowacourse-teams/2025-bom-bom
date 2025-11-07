package me.bombom.api.v1.article.repository;

import static me.bombom.api.v1.article.domain.QArticle.article;
import static me.bombom.api.v1.bookmark.domain.QBookmark.bookmark;
import static me.bombom.api.v1.newsletter.domain.QCategory.category;
import static me.bombom.api.v1.newsletter.domain.QNewsletter.newsletter;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.article.dto.response.ArticleCountPerNewsletterResponse;
import me.bombom.api.v1.article.dto.response.ArticleResponse;
import me.bombom.api.v1.article.dto.request.ArticlesOptionsRequest;
import me.bombom.api.v1.article.dto.response.QArticleResponse;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.CServerErrorException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.newsletter.dto.QNewsletterSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

@Slf4j
@RequiredArgsConstructor
public class ArticleRepositoryImpl implements CustomArticleRepository{

    private static final Map<String, Path<?>> SORT_FIELD_WHITELIST_MAP = Map.of(
            "title", article.title,
            "createdAt", article.createdAt,
            "arrivedDateTime", article.arrivedDateTime,
            "expectedReadTime", article.expectedReadTime
    );

    private static final int RECENT_DAYS = 3;

    private final JPAQueryFactory jpaQueryFactory;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<ArticleResponse> findArticles(
            Long memberId,
            ArticlesOptionsRequest options,
            Pageable pageable
    ) {
        if (StringUtils.hasText(options.keyword())) {
            return findArticlesWithUnion(memberId, options, pageable);
        }

        JPAQuery<Long> totalQuery = getTotalQuery(memberId, options);
        List<ArticleResponse> content = getContent(memberId, options, pageable);
        return PageableExecutionUtils.getPage(content, pageable, totalQuery::fetchOne);
    }

    private Page<ArticleResponse> findArticlesWithUnion(
            Long memberId,
            ArticlesOptionsRequest options,
            Pageable pageable
    ) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(RECENT_DAYS);
            String keyword = options.keyword().strip();

            // 각 쿼리에서 충분히 가져오기 위해 LIMIT을 pageSize * 2로 설정
            int fetchLimit = Math.max(pageable.getPageSize() * 2, 100);

            // 최근 3일 쿼리 실행
            String recentQuery = buildRecentQuery(options, cutoffDate, fetchLimit);
            Query recentQueryObj = entityManager.createNativeQuery(recentQuery);
            setQueryParameters(recentQueryObj, memberId, options, cutoffDate, keyword, fetchLimit);
            // 타임아웃 설정 (30초)
            recentQueryObj.setHint("jakarta.persistence.query.timeout", 30000);

            @SuppressWarnings("unchecked")
            List<Object[]> recentResults = recentQueryObj.getResultList();
            List<ArticleResponse> recentArticles = mapToArticleResponse(recentResults);

            // 과거 쿼리 실행
            String olderQuery = buildOlderQuery(options, cutoffDate, fetchLimit);
            Query olderQueryObj = entityManager.createNativeQuery(olderQuery);
            setQueryParameters(olderQueryObj, memberId, options, cutoffDate, keyword, fetchLimit);
            // 타임아웃 설정 (30초)
            olderQueryObj.setHint("jakarta.persistence.query.timeout", 30000);

            @SuppressWarnings("unchecked")
            List<Object[]> olderResults = olderQueryObj.getResultList();
            List<ArticleResponse> olderArticles = mapToArticleResponse(olderResults);

            // 애플리케이션에서 병합, 정렬, 최종 LIMIT 적용
            List<ArticleResponse> merged = Stream.concat(recentArticles.stream(), olderArticles.stream())
                    .sorted(Comparator.comparing(ArticleResponse::arrivedDateTime)
                            .thenComparing(ArticleResponse::articleId)
                            .reversed())
                    .limit(pageable.getPageSize())
                    .collect(Collectors.toList());

            final long totalCount = calculateTotalCount(memberId, options, cutoffDate, keyword, merged.size(), pageable.getPageSize());

            return PageableExecutionUtils.getPage(merged, pageable, () -> totalCount);
        } catch (Exception e) {
            log.error("검색 쿼리 실행 실패 - memberId: {}, keyword: '{}'", memberId, options.keyword(), e);
            throw new CServerErrorException(ErrorDetail.INTERNAL_SERVER_ERROR)
                    .addContext("memberId", memberId)
                    .addContext("keyword", options.keyword());
        }
    }

    private String buildRecentQuery(ArticlesOptionsRequest options, LocalDateTime cutoffDate, int limit) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ")
                .append("a.id AS article_id, ")
                .append("a.title, ")
                .append("a.contents_summary, ")
                .append("sr.arrived_date_time, ")
                .append("a.thumbnail_url, ")
                .append("a.expected_read_time, ")
                .append("a.is_read, ")
                .append("CASE WHEN b.article_id IS NOT NULL THEN 1 ELSE 0 END AS is_bookmarked, ")
                .append("n.name AS newsletter_name, ")
                .append("n.image_url AS newsletter_image_url, ")
                .append("c.name AS category_name ")
                .append("FROM search_recent sr ")
                .append("INNER JOIN article a ON a.id = sr.article_id ")
                .append("INNER JOIN newsletter n ON n.id = sr.newsletter_id ")
                .append("INNER JOIN category c ON c.id = n.category_id ")
                .append("LEFT JOIN bookmark b ON b.article_id = a.id AND b.member_id = :memberId ")
                .append("WHERE sr.member_id = :memberId ")
                .append("AND sr.arrived_date_time >= :cutoffDate ");

        if (options.newsletterId() != null) {
            sql.append("AND sr.newsletter_id = :newsletterId ");
        }

        if (options.date() != null) {
            sql.append("AND DATE(sr.arrived_date_time) = :date ");
        }

        sql.append("AND MATCH(sr.title, sr.contents_text) AGAINST(:keyword IN BOOLEAN MODE) ")
                .append("ORDER BY sr.arrived_date_time DESC, a.id DESC ")
                .append("LIMIT :limit");

        return sql.toString();
    }

    private String buildOlderQuery(ArticlesOptionsRequest options, LocalDateTime cutoffDate, int limit) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ")
                .append("a.id AS article_id, ")
                .append("a.title, ")
                .append("a.contents_summary, ")
                .append("a.arrived_date_time, ")
                .append("a.thumbnail_url, ")
                .append("a.expected_read_time, ")
                .append("a.is_read, ")
                .append("CASE WHEN b.article_id IS NOT NULL THEN 1 ELSE 0 END AS is_bookmarked, ")
                .append("n.name AS newsletter_name, ")
                .append("n.image_url AS newsletter_image_url, ")
                .append("c.name AS category_name ")
                .append("FROM article a ")
                .append("INNER JOIN newsletter n ON n.id = a.newsletter_id ")
                .append("INNER JOIN category c ON c.id = n.category_id ")
                .append("LEFT JOIN bookmark b ON b.article_id = a.id AND b.member_id = :memberId ")
                .append("WHERE a.member_id = :memberId ")
                .append("AND a.arrived_date_time < :cutoffDate ");

        if (options.newsletterId() != null) {
            sql.append("AND a.newsletter_id = :newsletterId ");
        }

        if (options.date() != null) {
            sql.append("AND DATE(a.arrived_date_time) = :date ");
        }

        sql.append("AND MATCH(a.title, a.contents_text) AGAINST(:keyword IN BOOLEAN MODE) ")
                .append("ORDER BY a.arrived_date_time DESC, a.id DESC ")
                .append("LIMIT :limit");

        return sql.toString();
    }

    private void setQueryParameters(
            Query query,
            Long memberId,
            ArticlesOptionsRequest options,
            LocalDateTime cutoffDate,
            String keyword,
            int limit
    ) {
        query.setParameter("memberId", memberId);
        query.setParameter("cutoffDate", cutoffDate);
        query.setParameter("keyword", keyword);
        query.setParameter("limit", limit);

        if (options.newsletterId() != null) {
            query.setParameter("newsletterId", options.newsletterId());
        }

        if (options.date() != null) {
            query.setParameter("date", options.date());
        }
    }

    private List<ArticleResponse> mapToArticleResponse(List<Object[]> results) {
        return results.stream()
                .map(row -> {
                    try {
                        Long articleId = extractLong(row[0]);
                        String title = extractString(row[1]);
                        String contentsSummary = extractString(row[2]);
                        LocalDateTime arrivedDateTime = extractLocalDateTime(row[3]);
                        String thumbnailUrl = extractString(row[4]);
                        Integer expectedReadTime = extractInteger(row[5], 0);
                        Boolean isRead = extractBoolean(row[6]);
                        Boolean isBookmarked = extractBoolean(row[7]);
                        String newsletterName = extractString(row[8]);
                        String newsletterImageUrl = extractString(row[9]);
                        String categoryName = extractString(row[10]);

                        if (articleId == null) {
                            return null;
                        }

                        return new ArticleResponse(
                                articleId,
                                title != null ? title : "",
                                contentsSummary != null ? contentsSummary : "",
                                arrivedDateTime,
                                thumbnailUrl,
                                expectedReadTime,
                                isRead,
                                isBookmarked,
                                new me.bombom.api.v1.newsletter.dto.NewsletterSummaryResponse(
                                        newsletterName != null ? newsletterName : "",
                                        newsletterImageUrl,
                                        categoryName != null ? categoryName : ""
                                )
                        );
                    } catch (Exception e) {
                        log.error("데이터 매핑 실패. row: {}", java.util.Arrays.toString(row), e);
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Long extractLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        if (obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private String extractString(Object obj) {
        if (obj == null) return null;
        if (obj instanceof String) {
            return (String) obj;
        }
        return obj.toString();
    }

    private LocalDateTime extractLocalDateTime(Object obj) {
        if (obj == null) return LocalDateTime.now();
        if (obj instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) obj).toLocalDateTime();
        }
        if (obj instanceof java.sql.Date) {
            return ((java.sql.Date) obj).toLocalDate().atStartOfDay();
        }
        if (obj instanceof java.time.LocalDateTime) {
            return (LocalDateTime) obj;
        }
        return LocalDateTime.now();
    }

    private Integer extractInteger(Object obj, int defaultValue) {
        if (obj == null) return defaultValue;
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        if (obj instanceof String) {
            try {
                return Integer.parseInt((String) obj);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private Boolean extractBoolean(Object obj) {
        if (obj == null) return false;
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        if (obj instanceof Number) {
            return ((Number) obj).intValue() == 1;
        }
        if (obj instanceof String) {
            String str = ((String) obj).trim();
            return "1".equals(str) || "true".equalsIgnoreCase(str) || "Y".equalsIgnoreCase(str);
        }
        return false;
    }

    private long calculateTotalCount(Long memberId, ArticlesOptionsRequest options, LocalDateTime cutoffDate, String keyword, int mergedSize, int pageSize) {
        try {
            long recentCount = getRecentCount(memberId, options, cutoffDate, keyword);
            long olderCount = getOlderCount(memberId, options, cutoffDate, keyword);
            return recentCount + olderCount;
        } catch (Exception e) {
            log.warn("카운트 쿼리 실패, 근사치 사용 - memberId: {}, keyword: {}", memberId, keyword, e);
            // 카운트 쿼리 실패 시 merged.size()로 근사치 사용
            return mergedSize + pageSize;
        }
    }

    private long getRecentCount(Long memberId, ArticlesOptionsRequest options, LocalDateTime cutoffDate, String keyword) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) ")
                .append("FROM search_recent sr ")
                .append("WHERE sr.member_id = :memberId ")
                .append("AND sr.arrived_date_time >= :cutoffDate ");

        if (options.newsletterId() != null) {
            sql.append("AND sr.newsletter_id = :newsletterId ");
        }

        if (options.date() != null) {
            sql.append("AND DATE(sr.arrived_date_time) = :date ");
        }

        sql.append("AND MATCH(sr.title, sr.contents_text) AGAINST(:keyword IN BOOLEAN MODE)");

        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("memberId", memberId);
        query.setParameter("cutoffDate", cutoffDate);
        query.setParameter("keyword", keyword);

        if (options.newsletterId() != null) {
            query.setParameter("newsletterId", options.newsletterId());
        }

        if (options.date() != null) {
            query.setParameter("date", options.date());
        }

        return ((Number) query.getSingleResult()).longValue();
    }

    private long getOlderCount(Long memberId, ArticlesOptionsRequest options, LocalDateTime cutoffDate, String keyword) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) ")
                .append("FROM article a ")
                .append("WHERE a.member_id = :memberId ")
                .append("AND a.arrived_date_time < :cutoffDate ");

        if (options.newsletterId() != null) {
            sql.append("AND a.newsletter_id = :newsletterId ");
        }

        if (options.date() != null) {
            sql.append("AND DATE(a.arrived_date_time) = :date ");
        }

        sql.append("AND MATCH(a.title, a.contents_text) AGAINST(:keyword IN BOOLEAN MODE)");

        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("memberId", memberId);
        query.setParameter("cutoffDate", cutoffDate);
        query.setParameter("keyword", keyword);

        if (options.newsletterId() != null) {
            query.setParameter("newsletterId", options.newsletterId());
        }

        if (options.date() != null) {
            query.setParameter("date", options.date());
        }

        return ((Number) query.getSingleResult()).longValue();
    }

    @Override
    public int countByMemberIdAndArrivedDateTimeAndIsRead(Long memberId, LocalDate date, boolean isRead) {
        Long count = jpaQueryFactory.select(article.count())
                .from(article)
                .where(createMemberWhereClause(memberId))
                .where(createDateWhereClause(date))
                .where(article.isRead.eq(isRead))
                .fetchOne();

        return Optional.ofNullable(count)
                .orElse(0L)
                .intValue();
    }

    @Override
    public List<ArticleCountPerNewsletterResponse> countPerNewsletter(Long memberId, String keyword) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        ArticleCountPerNewsletterResponse.class,
                        newsletter.id,
                        newsletter.name,
                        newsletter.imageUrl.coalesce(""),
                        article.id.count().castToNum(Integer.class)
                ))
                .from(article)
                .join(newsletter).on(newsletter.id.eq(article.newsletterId))
                .where(
                        article.memberId.eq(memberId),
                        createKeywordWhereClause(keyword)
                )
                .groupBy(newsletter.id, newsletter.name, newsletter.imageUrl)
                .orderBy(article.id.count().desc())
                .fetch();
    }


    private JPAQuery<Long> getTotalQuery(Long memberId, ArticlesOptionsRequest options) {
        return jpaQueryFactory.select(article.count())
                .from(article)
                .join(newsletter).on(article.newsletterId.eq(newsletter.id))
                .join(category).on(newsletter.categoryId.eq(category.id))
                .where(createMemberWhereClause(memberId))
                .where(createDateWhereClause(options.date()))
                .where(createKeywordWhereClause(options.keyword()))
                .where(createNewsletterIdWhereClause(options.newsletterId()));
    }

    private List<ArticleResponse> getContent(Long memberId, ArticlesOptionsRequest options, Pageable pageable) {
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
                .where(createKeywordWhereClause(options.keyword()))
                .where(createNewsletterIdWhereClause(options.newsletterId()))
                .orderBy(getOrderSpecifiers(pageable).toArray(OrderSpecifier[]::new))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
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

    private BooleanExpression createMemberWhereClause(Long memberId) {
        return article.memberId.eq(memberId);
    }

    private BooleanExpression createNewsletterIdWhereClause(Long newsletterId) {
        return Optional.ofNullable(newsletterId)
                .map(newsletter.id::eq)
                .orElse(null);
    }

    private BooleanExpression createDateWhereClause(LocalDate date) {
        return Optional.ofNullable(date)
                .map(d -> article.arrivedDateTime.between(
                        d.atTime(LocalTime.MIN),
                        d.atTime(LocalTime.MAX)))
                .orElse(null);
    }

    private BooleanExpression createKeywordWhereClause(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }

        String trimmed = "%" + keyword.strip() + "%";
        return article.title.like(trimmed)
                .or(article.contents.like(trimmed));
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
