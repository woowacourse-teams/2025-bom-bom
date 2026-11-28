package me.bombom.api.v1.notice.repository;

import java.util.Collection;
import java.util.List;
import me.bombom.api.v1.notice.domain.NoticeTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeTranslationRepository extends JpaRepository<NoticeTranslation, Long> {

    List<NoticeTranslation> findAllByNoticeIdInAndLanguage(Collection<Long> noticeIds, String language);
}
