package news.bombom.notification.repository;

import java.util.Optional;
import news.bombom.notification.domain.MemberNotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberNotificationSettingRepository extends JpaRepository<MemberNotificationSetting, Long> {

    Optional<MemberNotificationSetting> findByMemberId(Long memberId);
}
