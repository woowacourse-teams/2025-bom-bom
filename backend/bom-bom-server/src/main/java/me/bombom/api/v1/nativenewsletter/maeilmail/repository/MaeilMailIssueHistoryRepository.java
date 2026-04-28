package me.bombom.api.v1.nativenewsletter.maeilmail.repository;

import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailIssueHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaeilMailIssueHistoryRepository extends JpaRepository<MaeilMailIssueHistory, Long> {
}
