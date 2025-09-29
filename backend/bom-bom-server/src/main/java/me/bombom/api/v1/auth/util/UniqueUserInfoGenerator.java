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
    private static final String EMAIL_DOMAIN = "@bombom.news"; // 동일 도메인 정책
    private static final int NICKNAME_MAX_LENGTH = 20; // 컬럼/검증과 일치
    private static final int EMAIL_MAX_LENGTH = 50; // 컬럼과 일치 (전체 길이)

    private final UserInfoValidator userInfoValidator;

    public String getUniqueNickname(String nickname) {
        String normalizedNickname = nickname.strip().toLowerCase();
        if (userInfoValidator.isNicknameAvailable(normalizedNickname)) {
            return normalizedNickname;
        }
        return generateUniqueNickname(normalizedNickname);
    }

    public String getUniqueEmailLocalPart(String email) {
        String normalizedEmailLocalPart = email.strip().toLowerCase();
        String localPart = extractEmailLocalPart(normalizedEmailLocalPart);
        if (userInfoValidator.isEmailAvailable(normalizedEmailLocalPart)) {
            return localPart;
        }
        return generateUniqueEmailLocalPart(localPart);
    }

    private String generateUniqueNickname(String baseNickname) {
        String random = getRandomValue();
        String trimmedBase = trimNicknameForSuffix(baseNickname, random);
        String uniqueNickname = trimmedBase + NICKNAME_RANDOM_DELIMITER + random;
        log.debug("고유 닉네임 생성 - 원본: {}, 생성: {}", baseNickname, uniqueNickname);
        return uniqueNickname;
    }

    private String generateUniqueEmailLocalPart(String baseLocalPart) {
        String random = getRandomValue();
        String trimmedBase = trimEmailLocalPartForSuffix(baseLocalPart, random);
        String uniqueEmailLocalPart = trimmedBase + EMAIL_RANDOM_DELIMITER + random;
        log.debug("고유 이메일 로컬파트 생성 - 원본: {}, 생성: {}", baseLocalPart, uniqueEmailLocalPart);
        return uniqueEmailLocalPart;
    }

    private String extractEmailLocalPart(String email) {
        int atPos = email.indexOf('@');
        return atPos > 0 ? email.substring(0, atPos) : email;
    }

    private static String getRandomValue() {
        int randomValue = ThreadLocalRandom.current().nextInt(1, 10000);
        return String.format("%04d", randomValue);
    }

    private String trimNicknameForSuffix(String baseNickname, String random) {
        // (이메일 최대 길이 - 도메인 길이 - 랜덤값 추가로 붙는 길이)보다 사용자의 이메일이 길면 자름
        int availableLength = NICKNAME_MAX_LENGTH - NICKNAME_RANDOM_DELIMITER.length() - random.length();
        return baseNickname.length() > availableLength && availableLength > 0
                ? baseNickname.substring(0, availableLength)
                : baseNickname;
    }

    private String trimEmailLocalPartForSuffix(String baseLocalPart, String random) {
        // (닉네임 최대 길이 - 랜덤값 추가로 붙는 길이)보다 사용자의 닉네임이 길면 자름
        int localPartMaxLength = EMAIL_MAX_LENGTH - EMAIL_DOMAIN.length();
        int availableLength = localPartMaxLength - EMAIL_RANDOM_DELIMITER.length() - random.length();
        return baseLocalPart.length() > availableLength && availableLength > 0
                ? baseLocalPart.substring(0, availableLength)
                : baseLocalPart;
    }
}
