package me.bombom.api.v1.inquiry.repository;

import java.util.List;
import me.bombom.api.v1.inquiry.domain.InquiryCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryCategoryRepository extends JpaRepository<InquiryCategory, Long> {

    List<InquiryCategory> findAllByOrderByIdAsc();
}
