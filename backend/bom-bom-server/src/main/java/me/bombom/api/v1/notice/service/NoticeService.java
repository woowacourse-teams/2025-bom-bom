package me.bombom.api.v1.notice.service;

import java.util.Optional;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.newsletter.domain.NewsletterLanguage;
import me.bombom.api.v1.notice.domain.Notice;
import me.bombom.api.v1.notice.domain.NoticeTranslation;
import me.bombom.api.v1.notice.domain.NoticeVisibility;
import me.bombom.api.v1.notice.dto.NoticeResponse;
import me.bombom.api.v1.notice.repository.NoticeRepository;
import me.bombom.api.v1.notice.repository.NoticeTranslationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeTranslationRepository noticeTranslationRepository;

    public Page<NoticeResponse> getNotices(Pageable pageable, NewsletterLanguage language) {
        Page<Notice> notices = noticeRepository.findAllByVisibility(NoticeVisibility.PUBLIC, pageable);
        Map<Long, NoticeTranslation> translations = findTranslations(notices.getContent(), language);
        return notices.map(notice -> NoticeResponse.from(notice, translations.get(notice.getId())));
    }

    public Optional<NoticeResponse> getRepresentativeNotice(NewsletterLanguage language) {
        return noticeRepository.findRepresentativeByVisibility(NoticeVisibility.PUBLIC)
                .map(notice -> NoticeResponse.from(notice,
                        findTranslations(java.util.List.of(notice), language).get(notice.getId())));
    }

    private Map<Long, NoticeTranslation> findTranslations(java.util.List<Notice> notices, NewsletterLanguage language) {
        if (language == null || notices.isEmpty()) {
            return Map.of();
        }
        return noticeTranslationRepository.findAllByNoticeIdInAndLanguage(
                        notices.stream().map(Notice::getId).toList(), language.code())
                .stream()
                .collect(Collectors.toMap(NoticeTranslation::getNoticeId, Function.identity()));
    }
}
