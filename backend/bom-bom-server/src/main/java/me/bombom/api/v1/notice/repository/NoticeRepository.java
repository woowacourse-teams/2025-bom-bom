package me.bombom.api.v1.notice.repository;

import java.util.List;
import me.bombom.api.v1.notice.domain.Notice;
import me.bombom.api.v1.notice.dto.NoticeResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
}
