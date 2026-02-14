package news.bombom.captcha.service;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombom.captcha.client.GoogleRecaptchaClient;
import news.bombom.captcha.config.RecaptchaProperties;
import news.bombom.captcha.dto.response.CaptchaVerifyResponse;
import news.bombom.captcha.dto.response.GoogleRecaptchaResponse;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaService {

    private final GoogleRecaptchaClient googleRecaptchaClient;
    private final RecaptchaProperties recaptchaProperties;

    public CaptchaVerifyResponse verify(String gRecaptchaResponse, String remoteIp) {
        try {
            GoogleRecaptchaResponse response = googleRecaptchaClient.verify(gRecaptchaResponse, remoteIp);

            if (response == null) {
                log.error("reCAPTCHA 검증 실패: 응답이 null입니다.");
                return CaptchaVerifyResponse.fail("캡차 검증 중 오류가 발생했습니다.");
            }

            if (!response.isSuccess()) {
                log.warn("reCAPTCHA 검증 실패 - error-codes: {}", response.getErrorCodes());
                return CaptchaVerifyResponse.fail("캡차 검증에 실패했습니다.");
            }

            // 2분 만료 검증
            if (!validateNotExpired(response.getChallengeTs())) {
                log.warn("reCAPTCHA 검증 실패 - 토큰이 만료되었습니다. challenge_ts: {}", response.getChallengeTs());
                return CaptchaVerifyResponse.fail("캡차 토큰이 만료되었습니다.");
            }

            log.info("reCAPTCHA 검증 성공 - challenge_ts: {}, hostname: {}, remoteIp: {}",
                    response.getChallengeTs(), response.getHostname(), remoteIp);
            return CaptchaVerifyResponse.success();
        } catch (Exception e) {
            log.error("reCAPTCHA 검증 중 오류 발생: {}", e.getMessage(), e);
            return CaptchaVerifyResponse.fail("캡차 검증 중 오류가 발생했습니다.");
        }
    }

    /**
     * challenge_ts 검증 (2분 이내인지 확인)
     */
    private boolean validateNotExpired(String challengeTs) {
        if (challengeTs == null || challengeTs.isEmpty()) {
            log.warn("challenge_ts가 null이거나 비어있습니다.");
            return false;
        }

        try {
            Instant challengeTime = Instant.parse(challengeTs);
            Instant now = Instant.now();
            long secondsDiff = Duration.between(challengeTime, now).getSeconds();

            // 미래 시간 체크
            if (secondsDiff < 0) {
                log.warn("challenge_ts가 미래 시간입니다. challenge_ts: {}, 현재: {}", challengeTime, now);
                return false;
            }

            // 2분 이내인지 체크
            if (secondsDiff > recaptchaProperties.getMaxAgeSeconds()) {
                log.debug("토큰이 만료되었습니다. 경과 시간: {}초, 최대 허용: {}초", secondsDiff, recaptchaProperties.getMaxAgeSeconds());
                return false;
            }

            return true;
        } catch (DateTimeParseException e) {
            log.error("challenge_ts 파싱 실패: {}", challengeTs, e);
            return false;
        }
    }
}
