package me.bombom.api.v1.article.repository;

import java.util.Optional;
import me.bombom.api.v1.article.domain.WarningSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarningSettingRepository extends JpaRepository<WarningSetting, Long> {

    Optional<WarningSetting> findByMemberId(Long memberId);
}
