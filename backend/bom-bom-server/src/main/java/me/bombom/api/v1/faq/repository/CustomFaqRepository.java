package me.bombom.api.v1.faq.repository;

import me.bombom.api.v1.faq.domain.Faq;
import me.bombom.api.v1.faq.dto.GetFaqsRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomFaqRepository {

    Page<Faq> findFaqs(GetFaqsRequest request, Pageable pageable);
}
