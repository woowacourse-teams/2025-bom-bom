package me.bombom.api.v1.member.repository;

import java.util.Optional;
import me.bombom.api.v1.member.domain.WarningSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarningSettingRepository extends JpaRepository<WarningSetting, Long> {

    Optional<WarningSetting> findByMemberId(Long memberId);
}
