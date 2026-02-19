package me.bombom.api.v1.admin.lambda.repository;

import me.bombom.api.v1.admin.lambda.domain.UnsubscribePattern;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnsubscribePatternRepository extends JpaRepository<UnsubscribePattern, Long> {
}
