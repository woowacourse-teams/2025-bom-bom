package me.bombom.api.v1.article.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.dto.ArticleResponse;
import me.bombom.api.v1.article.dto.GetArticlesOptions;
import me.bombom.api.v1.article.enums.SortOption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class ArticleRepositoryImpl implements CustomArticleRepository{

    private final EntityManager entityManager;

    @Override
    public Page<ArticleResponse> findByMemberId(
            Long memberId,
            GetArticlesOptions options,
            Pageable pageable
    ) {
        Long total = getTotalCount(memberId, options);
        List<ArticleResponse> content = getArticleContents(memberId, options, pageable);
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public int countAllByMemberId(Long memberId, String keyword) {
        StringBuilder jpql = new StringBuilder("""
                SELECT COUNT(a)
                FROM Article a
                WHERE a.memberId = :memberId
        """);
        addKeywordFilter(jpql, keyword);

        TypedQuery<Long> countQuery = entityManager.createQuery(jpql.toString(), Long.class);
        addMemberIdParameter(countQuery, memberId);
        addKeywordParameter(countQuery, keyword);
        return countQuery.getSingleResult().intValue();
    }

    @Override
    public int countAllByCategoryIdAndMemberId(Long memberId, Long categoryId, String keyword) {
        StringBuilder jpql = new StringBuilder("""
                SELECT COUNT(a)
                FROM Article a
                JOIN Newsletter n ON n.id = a.newsletterId
                JOIN Category c ON c.id = n.categoryId 
                WHERE a.memberId = :memberId
        """);
        addKeywordFilter(jpql, keyword);
        addCategoryFilter(jpql, categoryId);

        TypedQuery<Long> countQuery = entityManager.createQuery(jpql.toString(), Long.class);
        addMemberIdParameter(countQuery, memberId);
        addKeywordParameter(countQuery, keyword);
        addCategoryIdParameter(countQuery, categoryId);
        return countQuery.getSingleResult().intValue();
    }

    private Long getTotalCount(Long memberId, GetArticlesOptions options) {
        StringBuilder jpql = new StringBuilder("""
                SELECT COUNT(a)
                FROM Article a
                JOIN Newsletter n ON n.id = a.newsletterId
                JOIN Category c ON c.id = n.categoryId
                WHERE a.memberId = :memberId
        """);
        appendDynamicWhereClause(
                jpql,
                options.date(),
                options.categoryId(),
                options.keyword()
        );

        TypedQuery<Long> countQuery = entityManager.createQuery(jpql.toString(), Long.class);
        setQueryParameters(
                countQuery,
                memberId,
                options.date(),
                options.categoryId(),
                options.keyword()
        );
        return countQuery.getSingleResult();
    }

    private List<ArticleResponse> getArticleContents(
            Long memberId,
            GetArticlesOptions options,
            Pageable pageable
    ) {
        StringBuilder jpql = new StringBuilder("""
                SELECT new me.bombom.api.v1.article.dto.ArticleResponse(
                        a.id, a.title, a.contentsSummary, a.arrivedDateTime, a.thumbnailUrl, a.expectedReadTime, a.isRead,
                        new me.bombom.api.v1.newsletter.dto.NewsletterSummaryResponse(n.name, n.imageUrl, c.name))
                FROM Article a
                JOIN Newsletter n ON n.id = a.newsletterId
                JOIN Category c ON c.id = n.categoryId
                WHERE a.memberId = :memberId
        """);
        appendDynamicWhereClause(
                jpql,
                options.date(),
                options.categoryId(),
                options.keyword()
        );
        appendOrderByClause(options.sorted(), jpql);

        TypedQuery<ArticleResponse> query = entityManager.createQuery(jpql.toString(), ArticleResponse.class);
        setQueryParameters(
                query,
                memberId,
                options.date(),
                options.categoryId(),
                options.keyword()
        );

        if (pageable.isPaged()) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }

        return query.getResultList();
    }

    private void appendDynamicWhereClause(
            StringBuilder jpql,
            LocalDate date,
            Long categoryId,
            String keyword
    ) {
        addDateFilter(jpql, date);
        addCategoryFilter(jpql, categoryId);
        addKeywordFilter(jpql, keyword);
    }

    private void addDateFilter(StringBuilder jpql, LocalDate date) {
        if (date != null) {
            jpql.append(" AND a.arrivedDateTime BETWEEN :dateAtMinTime AND :dateAtMaxTime");
        }
    }

    private void addCategoryFilter(StringBuilder jpql, Long categoryId) {
        if (categoryId != null) {
            jpql.append(" AND n.categoryId = :categoryId");
        }
    }

    private void addKeywordFilter(StringBuilder jpql, String keyword) {
        if (StringUtils.hasText(keyword)) {
            jpql.append(" AND a.title LIKE :keyword");
        }
    }

    private void appendOrderByClause(SortOption sortOption, StringBuilder jpql) {
        jpql.append(" ORDER BY a.arrivedDateTime ").append(sortOption.name());
    }

    private void setQueryParameters(
            TypedQuery<?> query,
            Long memberId,
            LocalDate date,
            Long categoryId,
            String keyword
    ) {
        addMemberIdParameter(query, memberId);
        addDateParameter(query, date);
        addCategoryIdParameter(query, categoryId);
        addKeywordParameter(query, keyword);
    }

    private void addMemberIdParameter(TypedQuery<?> query, Long memberId) {
        query.setParameter("memberId", memberId);
    }

    private void addDateParameter(TypedQuery<?> query, LocalDate date) {
        if (date != null) {
            query.setParameter("dateAtMinTime", date.atTime(LocalTime.MIN));
            query.setParameter("dateAtMaxTime", date.atTime(LocalTime.MAX));
        }
    }

    private void addCategoryIdParameter(TypedQuery<?> query, Long categoryId) {
        if (categoryId != null) {
            query.setParameter("categoryId", categoryId);
        }
    }

    private void addKeywordParameter(TypedQuery<?> query, String keyword) {
        if (StringUtils.hasText(keyword)) {
            query.setParameter("keyword", "%" + keyword.trim() + "%");
        }
    }
}
