package me.bombom.api.v1.auth.util;

import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 고유값 생성 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UniqueUserInfoGenerator {

    public static final String NICKNAME_RANDOM_DELIMITER = "#";
    public static final String EMAIL_RANDOM_DELIMITER = ".";

    private final UserInfoValidator userInfoValidator;

    public String getUniqueNickname(String nickname) {
        String normalizedNickname = nickname.strip().toLowerCase();
        if (userInfoValidator.isNicknameAvailable(normalizedNickname)) {
            return normalizedNickname;
        }
        return generateUniqueNickname(normalizedNickname);
    }

    public String getUniqueEmail(String email) {
        String normalizedEmail = email.strip().toLowerCase();
        String localPart = extractEmailLocalPart(normalizedEmail);
        if (userInfoValidator.isEmailAvailable(normalizedEmail)) {
            return localPart;
        }
        return generateUniqueEmailLocalPart(localPart);
    }

    private String generateUniqueNickname(String baseNickname) {
        String uniqueNickname = baseNickname + NICKNAME_RANDOM_DELIMITER + getRandomValue();
        log.debug("고유 닉네임 생성 - 원본: {}, 생성: {}", baseNickname, uniqueNickname);
        return uniqueNickname;
    }

    private String generateUniqueEmailLocalPart(String baseLocalPart) {
        String uniqueEmailLocalPart = baseLocalPart + EMAIL_RANDOM_DELIMITER + getRandomValue();
        log.debug("고유 이메일 로컬파트 생성 - 원본: {}, 생성: {}", baseLocalPart, uniqueEmailLocalPart);
        return uniqueEmailLocalPart;
    }

    private String extractEmailLocalPart(String email) {
        int atPos = email.indexOf('@');
        return atPos > 0 ? email.substring(0, atPos) : email;
    }

    private static String getRandomValue() {
        int randomValue = ThreadLocalRandom.current().nextInt(10, 10000);
        return String.valueOf(randomValue);
    }
}
