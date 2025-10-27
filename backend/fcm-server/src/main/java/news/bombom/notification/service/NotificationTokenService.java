package news.bombom.notification.service;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombom.notification.domain.MemberFcmToken;
import news.bombom.notification.repository.MemberFcmTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationTokenService {

    private final MemberFcmTokenRepository fcmTokenRepository;

    /**
     * 알림 토큰 등록
     */
    @Transactional
    public void registerFcmToken(Long memberId, String deviceUuid, String fcmToken) {
        fcmTokenRepository.deleteByDeviceUuid(deviceUuid);

        MemberFcmToken token = MemberFcmToken.builder()
                .memberId(memberId)
                .deviceUuid(deviceUuid)
                .fcmToken(fcmToken)
                .isNotificationEnabled(true)
                .build();
        fcmTokenRepository.save(token);
        log.info("알림 토큰 등록 완료: memberId={}, deviceUuid={}", memberId, deviceUuid);
    }

    /**
     * 알림 토큰 업데이트 또는 등록
     */
    @Transactional
    public void upsertFcmToken(Long memberId, String deviceUuid, String fcmToken) {
        Optional<MemberFcmToken> fcmTokenOptional = fcmTokenRepository.findByMemberIdAndDeviceUuid(memberId,
                deviceUuid);

        if (fcmTokenOptional.isPresent()) {
            MemberFcmToken token = fcmTokenOptional.get();
            token.updateToken(fcmToken);
            return;
        }

        fcmTokenRepository.deleteByDeviceUuid(deviceUuid);
        MemberFcmToken token = MemberFcmToken.builder()
                .memberId(memberId)
                .deviceUuid(deviceUuid)
                .fcmToken(fcmToken)
                .isNotificationEnabled(true)
                .build();
        fcmTokenRepository.save(token);
        log.info("알림 토큰 처리 완료: memberId={}, deviceUuid={}", memberId, deviceUuid);
    }

    /**
     * 알림 설정 업데이트
     */
    @Transactional
    public void updateNotificationSetting(Long memberId, String deviceUuid, boolean enabled) {
        Optional<MemberFcmToken> existingToken = fcmTokenRepository.findByMemberIdAndDeviceUuid(memberId, deviceUuid);

        if (existingToken.isPresent()) {
            MemberFcmToken fcmToken = existingToken.get();
            fcmToken.updateNotificationSetting(enabled);
            log.info("알림 설정 변경: memberId={}, deviceUuid={}, enabled={}", memberId, deviceUuid, enabled);
        } else {
            log.warn("알림 토큰을 찾을 수 없습니다: memberId={}, deviceUuid={}", memberId, deviceUuid);
        }
    }

    /**
     * 알림 토큰 삭제
     */
    @Transactional
    public void unregisterFcmToken(Long memberId, String deviceUuid) {
        fcmTokenRepository.deleteByMemberIdAndDeviceUuid(memberId, deviceUuid);
        log.info("알림 토큰 삭제: memberId={}, deviceUuid={}", memberId, deviceUuid);
    }

    /**
     * 회원의 알림 토큰 조회
     */
    public List<MemberFcmToken> resolveTokens(Long memberId) {
        List<MemberFcmToken> fcmTokens = fcmTokenRepository.findByMemberId(memberId);

        if (fcmTokens.isEmpty()) {
            log.warn("FCM 토큰이 없습니다: memberId={}", memberId);
        }

        return fcmTokens;
    }
}
