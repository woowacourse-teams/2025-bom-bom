package me.bombom.api.v1.faq.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.faq.dto.FaqResponse;
import me.bombom.api.v1.faq.repository.FaqRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FaqService {

    private final FaqRepository faqRepository;

    public Page<FaqResponse> getFaqs(Pageable pageable) {
        return faqRepository.findAll(pageable)
                .map(FaqResponse::from);
    }
}
