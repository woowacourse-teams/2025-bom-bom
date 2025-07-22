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
        if (date != null) {
            jpql.append(" AND a.arrivedDateTime BETWEEN :dateAtMinTime AND :dateAtMaxTime");
        }
        if (categoryId != null) {
            jpql.append(" AND n.categoryId = :categoryId");
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
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
        query.setParameter("memberId", memberId);
        if (date != null) {
            query.setParameter("dateAtMinTime", date.atTime(LocalTime.MIN));
            query.setParameter("dateAtMaxTime", date.atTime(LocalTime.MAX));
        }
        if (categoryId != null) {
            query.setParameter("categoryId", categoryId);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            query.setParameter("keyword", "%" + keyword.trim() + "%");
        }
    }
}
