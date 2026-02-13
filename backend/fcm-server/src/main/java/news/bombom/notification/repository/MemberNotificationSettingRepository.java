package news.bombom.notification.repository;

import java.util.List;
import java.util.Optional;
import news.bombom.notification.domain.MemberNotificationSetting;
import news.bombom.notification.domain.NotificationCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberNotificationSettingRepository extends JpaRepository<MemberNotificationSetting, Long> {

    List<MemberNotificationSetting> findAllByMemberId(Long memberId);

    Optional<MemberNotificationSetting> findByMemberIdAndCategory(Long memberId, NotificationCategory category);

    boolean existsByMemberIdAndCategory(Long memberId, NotificationCategory category);
}
