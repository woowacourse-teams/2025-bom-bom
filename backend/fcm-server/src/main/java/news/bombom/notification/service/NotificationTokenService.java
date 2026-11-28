package news.bombom.notification.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Objects;
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
    public void registerToken(Long memberId, String deviceUuid, String fcmToken) {
        replaceDeviceToken(memberId, deviceUuid, fcmToken);
        log.info("알림 토큰 등록 완료: memberId={}, deviceUuid={}", memberId, deviceUuid);
    }

    /**
     * 알림 토큰 업데이트 또는 등록
     */
    @Transactional
    public void upsertToken(Long memberId, String deviceUuid, String fcmToken) {
        Optional<MemberFcmToken> fcmTokenOptional = fcmTokenRepository.findByMemberIdAndDeviceUuid(memberId, deviceUuid);

        if (fcmTokenOptional.isPresent()) {
            MemberFcmToken token = fcmTokenOptional.get();
            token.updateToken(fcmToken);
            return;
        }

        replaceDeviceToken(memberId, deviceUuid, fcmToken);
        log.info("알림 토큰 처리 완료: memberId={}, deviceUuid={}", memberId, deviceUuid);
    }

    /**
     * 기기의 기존 토큰을 지우고 새로 만든다. OS 알림 권한은 회원이 아니라 기기 설정이라 그대로 옮긴다.
     */
    private void replaceDeviceToken(Long memberId, String deviceUuid, String fcmToken) {
        Boolean osPermissionGranted = fcmTokenRepository.findByDeviceUuid(deviceUuid).stream()
                .map(MemberFcmToken::getOsPermissionGranted)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        fcmTokenRepository.deleteByDeviceUuid(deviceUuid);

        MemberFcmToken token = MemberFcmToken.builder()
                .memberId(memberId)
                .deviceUuid(deviceUuid)
                .fcmToken(fcmToken)
                .isNotificationEnabled(true)
                .build();
        if (osPermissionGranted != null) {
            token.updateOsPermission(osPermissionGranted);
        }
        fcmTokenRepository.save(token);
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
     * 기기 OS 알림 권한 동기화
     */
    @Transactional
    public void updateOsPermission(Long memberId, String deviceUuid, boolean granted) {
        Optional<MemberFcmToken> existingToken = fcmTokenRepository.findByMemberIdAndDeviceUuid(memberId, deviceUuid);

        if (existingToken.isPresent()) {
            existingToken.get().updateOsPermission(granted);
            log.info("OS 알림 권한 동기화: memberId={}, deviceUuid={}, granted={}", memberId, deviceUuid, granted);
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

    /**
     * 회원의 FCM 토큰 문자열 리스트 조회
     */
    public List<String> getTokenStrings(Long memberId) {
        return resolveTokens(memberId).stream()
                .map(MemberFcmToken::getFcmToken)
                .toList();
    }

    public boolean getNotificationEnabled(Long memberId, String deviceUuid) {
        MemberFcmToken memberFcmToken = fcmTokenRepository.findByMemberIdAndDeviceUuid(memberId, deviceUuid)
                .orElseThrow(() -> {
                    log.warn("FCM 토큰 조회 실패: memberId={}, deviceUuid={}", memberId, deviceUuid);
                    return new EntityNotFoundException(
                            String.format(
                                    "memberId=%d, deviceUuid=%s 에 해당하는 FCM 토큰이 존재하지 않습니다.",
                                    memberId,
                                    deviceUuid));
                });
        return memberFcmToken.isNotificationEnabled();
    }
}
