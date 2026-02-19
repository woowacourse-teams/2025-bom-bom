package me.bombom.api.v1.subscribe.repository;

import me.bombom.api.v1.subscribe.domain.UnsubscribePattern;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnsubscribePatternRepository extends JpaRepository<UnsubscribePattern, Long> {
}
