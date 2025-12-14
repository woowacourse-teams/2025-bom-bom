package me.bombom.api.v1.notice.repository;

import java.util.List;
import me.bombom.api.v1.notice.domain.Notice;
import me.bombom.api.v1.notice.dto.NoticeResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    @Query("""
        SELECT new me.bombom.api.v1.notice.dto.NoticeResponse(
            n.id,
            nc.name,
            n.title,
            n.content,
            n.createdAt
        )
        FROM Notice n
        JOIN NoticeCategory nc ON n.noticeCategoryId = nc.id
        ORDER BY n.createdAt DESC
    """)
    List<NoticeResponse> findAllNotices();
}
