package me.bombom.api.v1.article.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.dto.ArticleResponse;
import me.bombom.api.v1.article.enums.SortOption;

@RequiredArgsConstructor
public class ArticleRepositoryImpl implements CustomArticleRepository{

    private final EntityManager entityManager;

    @Override
    public List<ArticleResponse> findByMemberId(
            Long memberId,
            LocalDate date,
            Long categoryId,
            SortOption sortOption
    ) {
        StringBuilder jpql = new StringBuilder("""
            SELECT new me.bombom.api.v1.article.dto.ArticleResponse(
                a.id, a.title, a.contentsSummary, a.arrivedDateTime, a.thumbnailUrl, a.expectedReadTime, a.isRead,
                new me.bombom.api.v1.newsletter.dto.TodayArticleNewsletterResponse(
                    n.name, n.imageUrl, n.categoryId))
            FROM Article a
            JOIN Newsletter n ON n.id = a.newsletterId
            WHERE a.memberId = :memberId
        """);
        if (date != null) {
            jpql.append(" AND a.arrivedDateTime >= :dateTime");
        }
        if (categoryId != null) {
            jpql.append(" AND n.categoryId = :categoryId");
        }
        jpql.append(" ORDER BY a.arrivedDateTime ").append(sortOption.getValue());

        TypedQuery<ArticleResponse> query =
                entityManager.createQuery(jpql.toString(), ArticleResponse.class);
        query.setParameter("memberId", memberId);
        if (date != null) {
            query.setParameter("dateTime", date.atStartOfDay());
        }
        if (categoryId != null) {
            query.setParameter("categoryId", categoryId);
        }
        return query.getResultList();
    }
}
