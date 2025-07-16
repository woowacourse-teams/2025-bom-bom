package me.bombom.api.v1.article.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.dto.ArticleResponse;
import me.bombom.api.v1.article.enums.SortOption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class ArticleRepositoryImpl implements CustomArticleRepository{

    private final EntityManager entityManager;

    @Override
    public Page<ArticleResponse> findByMemberId(
            Long memberId,
            LocalDate date,
            Long categoryId,
            SortOption sortOption,
            Pageable pageable
    ) {
        Long total = getTotalCount(memberId, date, categoryId);
        List<ArticleResponse> content = getArticleContents(memberId, date, categoryId, sortOption, pageable);
        return new PageImpl<>(content, pageable, total);
    }

    private Long getTotalCount(
            Long memberId,
            LocalDate date,
            Long categoryId
    ) {
        StringBuilder jpql = new StringBuilder("""
                SELECT COUNT(a)
                FROM Article a
                JOIN Newsletter n ON n.id = a.newsletterId
                JOIN Category c ON c.id = n.categoryId
                WHERE a.memberId = :memberId
        """);
        appendDynamicWhereClause(date, categoryId, jpql);

        TypedQuery<Long> countQuery = entityManager.createQuery(jpql.toString(), Long.class);
        setQueryParameters(
                countQuery,
                memberId,
                date,
                categoryId
        );
        return countQuery.getSingleResult();
    }

    private List<ArticleResponse> getArticleContents(
            Long memberId,
            LocalDate date,
            Long categoryId,
            SortOption sortOption,
            Pageable pageable
    ) {
        StringBuilder jpql = new StringBuilder("""
                SELECT new me.bombom.api.v1.article.dto.ArticleResponse(
                        a.id, a.title, a.contentsSummary, a.arrivedDateTime, a.thumbnailUrl, a.expectedReadTime, a.isRead,
                        new me.bombom.api.v1.newsletter.dto.TodayArticleNewsletterResponse(n.name, n.imageUrl, c.name))
                FROM Article a
                JOIN Newsletter n ON n.id = a.newsletterId
                JOIN Category c ON c.id = n.categoryId
                WHERE a.memberId = :memberId
        """);
        appendDynamicWhereClause(date, categoryId, jpql);
        appendOrderByClause(sortOption, jpql);

        TypedQuery<ArticleResponse> query = entityManager.createQuery(jpql.toString(), ArticleResponse.class);
        setQueryParameters(query, memberId, date, categoryId);

        if (pageable.isPaged()) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }

        return query.getResultList();
    }

    private void setQueryParameters(
            TypedQuery<?> query,
            Long memberId,
            LocalDate date,
            Long categoryId
    ) {
        query.setParameter("memberId", memberId);
        if (date != null) {
            query.setParameter("dateAtMinTime", date.atTime(LocalTime.MIN));
            query.setParameter("dateAtMaxTime", date.atTime(LocalTime.MAX));
        }
        if (categoryId != null) {
            query.setParameter("categoryId", categoryId);
        }
    }

    private static void appendOrderByClause(SortOption sortOption, StringBuilder jpql) {
        jpql.append(" ORDER BY a.arrivedDateTime ").append(sortOption.getValue());
    }

    private static void appendDynamicWhereClause(LocalDate date, Long categoryId, StringBuilder jpql) {
        if (date != null) {
            jpql.append(" AND a.arrivedDateTime >= :dateAtMinTime");
            jpql.append(" AND a.arrivedDateTime <= :dateAtMaxTime");
        }
        if (categoryId != null) {
            jpql.append(" AND n.categoryId = :categoryId");
        }
    }
}
