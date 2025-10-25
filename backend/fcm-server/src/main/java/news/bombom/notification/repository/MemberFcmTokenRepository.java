package news.bombom.notification.repository;

import java.util.List;
import java.util.Optional;
import news.bombom.notification.domain.MemberFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberFcmTokenRepository extends JpaRepository<MemberFcmToken, Long> {

    List<MemberFcmToken> findByMemberId(Long memberId);

    Optional<MemberFcmToken> findByMemberIdAndDeviceUuid(Long memberId, String deviceUuid);

    void deleteByMemberIdAndDeviceUuid(Long memberId, String deviceUuid);

    void deleteByDeviceUuid(String deviceUuid);
}
