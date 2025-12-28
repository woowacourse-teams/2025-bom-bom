package me.bombom.api.v1.article.repository;

import java.util.List;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.dto.response.ArticleCountPerNewsletterResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleRepository extends JpaRepository<Article, Long>, CustomArticleRepository {

    List<ArticleCountPerNewsletterResponse> countPerNewsletter(@Param("memberId") Long memberId, @Param("keyword") String keyword);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Article a WHERE a.memberId = :memberId")
    void deleteAllByMemberId(Long memberId);

    long countByIdInAndMemberId(List<Long> ids, Long memberId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Article a WHERE a.id IN :ids AND a.memberId = :memberId")
    void deleteAllByIdsAndMemberId(@Param("ids") List<Long> ids, @Param("memberId") Long memberId);

    /**
     * 관리자의 모든 article을 previous_article로 복사
     * - 중복 방지: 이미 있으면 스킵
     */
    @Modifying
    @Query(value = """
        INSERT INTO previous_article (
            title, contents, contents_summary,
            expected_read_time, newsletter_id, arrived_date_time,
            is_fixed
        )
        SELECT 
            a.title, 
            a.contents, 
            a.contents_summary,
            a.expected_read_time, 
            a.newsletter_id, 
            a.arrived_date_time,
            false
        FROM article a
        WHERE a.member_id = :adminId
        AND NOT EXISTS (
            SELECT 1 FROM previous_article pa
            WHERE pa.newsletter_id = a.newsletter_id
            AND pa.title = a.title
            AND pa.arrived_date_time = a.arrived_date_time
        )
    """, nativeQuery = true)
    int safeCopyToArchive(@Param("adminId") Long adminId);

    /**
     * 뉴스레터별로 최신 N개만 남기고 나머지 삭제
     * - previous_article로 복사 확인 후 삭제
     */
    @Modifying
    @Query(value = """
        DELETE a FROM article a
        WHERE a.id IN (
            SELECT id FROM (
                SELECT id,
                       ROW_NUMBER() OVER (
                           PARTITION BY newsletter_id 
                           ORDER BY arrived_date_time DESC, id DESC
                       ) AS row_num
                FROM article
                WHERE member_id = :adminId
            ) ranked
            WHERE ranked.row_num > :keepCount
        )
        AND EXISTS (
            SELECT 1 FROM previous_article pa
            WHERE pa.newsletter_id = a.newsletter_id
            AND pa.title = a.title
            AND pa.arrived_date_time = a.arrived_date_time
        )
    """, nativeQuery = true)
    int safeDeleteArchived(
            @Param("adminId") Long adminId,
            @Param("keepCount") int keepCount
    );

    @Modifying
    @Query(value = """
        DELETE a
        FROM article a
        JOIN (
            SELECT id
            FROM (
                SELECT a.id,
                       ROW_NUMBER() OVER (
                           PARTITION BY a.member_id 
                           ORDER BY a.arrived_date_time DESC, a.id DESC
                       ) AS row_num,
                        GREATEST(
                            CASE
                                WHEN r.authority = 'ADMIN' THEN :adminLimit
                                WHEN r.authority = 'USER'  THEN :userLimit
                            END,
                            500
                        ) AS keep_limit
                FROM article a
                JOIN member m ON m.id = a.member_id
                JOIN role r ON r.id = m.role_id
                LEFT JOIN bookmark b ON b.article_id = a.id AND b.member_id = a.member_id
                WHERE b.id IS NULL
                  AND r.authority IN ('ADMIN', 'USER')
            ) ranked
            WHERE ranked.row_num > ranked.keep_limit
        ) target ON target.id = a.id
    """, nativeQuery = true)
    int deleteExcessUnbookmarkedArticles(@Param("adminLimit") int adminLimit, @Param("userLimit") int userLimit);
}
