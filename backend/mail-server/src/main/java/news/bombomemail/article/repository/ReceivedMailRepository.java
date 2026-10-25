package news.bombomemail.article.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import news.bombomemail.article.domain.ReceivedMail;
import news.bombomemail.article.domain.ReceivedMailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReceivedMailRepository extends JpaRepository<ReceivedMail, ReceivedMailId> {

    @Query(value = """
            SELECT article_id
            FROM received_mail
            WHERE member_id = :memberId
              AND message_id_hash = :messageIdHash
            """, nativeQuery = true)
    Optional<Long> findArticleId(@Param("memberId") Long memberId, @Param("messageIdHash") byte[] messageIdHash);

    /**
     * 같은 키가 있으면 PK 충돌로 실패한다. 두 서버가 같은 메일을 동시에 저장해도 한쪽만 통과한다.
     */
    @Modifying
    @Query(value = """
            INSERT INTO received_mail (member_id, message_id_hash, article_id, created_at)
            VALUES (:memberId, :messageIdHash, :articleId, :createdAt)
            """, nativeQuery = true)
    void insert(
            @Param("memberId") Long memberId,
            @Param("messageIdHash") byte[] messageIdHash,
            @Param("articleId") Long articleId,
            @Param("createdAt") LocalDateTime createdAt
    );

    @Modifying
    @Query(value = """
            DELETE FROM received_mail
            WHERE created_at < :cutoff
            LIMIT :limit
            """, nativeQuery = true)
    int deleteCreatedBefore(@Param("cutoff") LocalDateTime cutoff, @Param("limit") int limit);
}
