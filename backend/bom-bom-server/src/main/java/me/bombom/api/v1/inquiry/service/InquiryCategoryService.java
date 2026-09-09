package me.bombom.api.v1.inquiry.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.inquiry.dto.response.InquiryCategoryResponse;
import me.bombom.api.v1.inquiry.repository.InquiryCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryCategoryService {

    private final InquiryCategoryRepository inquiryCategoryRepository;

    public List<InquiryCategoryResponse> getCategories() {
        return inquiryCategoryRepository.findAllByOrderByIdAsc().stream()
                .map(InquiryCategoryResponse::from)
                .toList();
    }
}
