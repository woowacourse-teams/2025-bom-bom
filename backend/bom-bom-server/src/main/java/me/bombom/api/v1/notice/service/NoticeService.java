package me.bombom.api.v1.notice.service;

import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.notice.domain.Notice;
import me.bombom.api.v1.notice.dto.NoticeResponse;
import me.bombom.api.v1.notice.repository.NoticeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public List<NoticeResponse> getNotices() {
        return noticeRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Notice::getCreatedAt).reversed())
                .map(NoticeResponse::from)
                .toList();
    }
}
