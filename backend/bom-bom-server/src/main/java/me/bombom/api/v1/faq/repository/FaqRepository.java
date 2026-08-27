package me.bombom.api.v1.faq.repository;

import me.bombom.api.v1.faq.domain.Faq;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaqRepository extends JpaRepository<Faq, Long> {
}
