package me.bombom.api.v1.notice.repository;

import java.util.Optional;
import me.bombom.api.v1.notice.domain.Notice;
import me.bombom.api.v1.notice.domain.NoticeVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    Page<Notice> findAllByVisibility(NoticeVisibility visibility, Pageable pageable);

    @Query("""
            select n
            from Notice n
            join NoticeRepresentative r on r.noticeId = n.id
            where n.visibility = :visibility
            """)
    Optional<Notice> findRepresentativeByVisibility(@Param("visibility") NoticeVisibility visibility);
}
