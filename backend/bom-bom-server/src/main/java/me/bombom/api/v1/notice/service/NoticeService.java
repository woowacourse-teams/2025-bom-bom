package me.bombom.api.v1.notice.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.notice.domain.NoticeVisibility;
import me.bombom.api.v1.notice.dto.NoticeResponse;
import me.bombom.api.v1.notice.repository.NoticeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public Page<NoticeResponse> getNotices(Pageable pageable) {
        return noticeRepository.findAllByVisibility(NoticeVisibility.PUBLIC, pageable)
                .map(NoticeResponse::from);
    }

    public Optional<NoticeResponse> getRepresentativeNotice() {
        return noticeRepository.findRepresentativeByVisibility(NoticeVisibility.PUBLIC)
                .map(NoticeResponse::from);
    }
}
