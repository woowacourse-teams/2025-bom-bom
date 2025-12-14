package me.bombom.api.v1.notice.repository;

import me.bombom.api.v1.notice.domain.NoticeCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeCategoryRepository extends JpaRepository<NoticeCategory, Long> {
}
