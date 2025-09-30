package me.bombom.api.v1.auth.util;

import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 사용자 정보 검증 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserInfoValidator {

    /**
     * 닉네임 검증 정규식
     * - 영문/숫자/한글로 시작하고 끝남 (최소 2글자)
     * - 중간에 마침표, 언더스코어, 공백 허용
     * - 연속된 마침표(..) 금지
     * - 선택적으로 '#숫자'가 붙을 수 있음
     */
    public static final String NICKNAME_REGEX_PATTERN = "^(?!.*\\.\\.)[A-Za-z0-9가-힣](?:[A-Za-z0-9가-힣._ ]*[A-Za-z0-9가-힣])?(?:#\\d+)?$";
    public static final String EMAIL_REGEX_PATTERN = "^[a-zA-Z0-9](?:[a-zA-Z0-9._-]*[a-zA-Z0-9])?@bombom\\.news$";
    private static final String EMAIL_DOMAIN = "@bombom.news";
    public static final int EMAIL_MIN_LENGTH = 15;
    public static final int EMAIL_MAX_LENGTH = 50;
    public static final int NICKNAME_MIN_LENGTH = 2;
    public static final int NICKNAME_MAX_LENGTH = 20;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX_PATTERN);
    private static final Pattern NICKNAME_PATTERN = Pattern.compile(NICKNAME_REGEX_PATTERN);

    private final MemberRepository memberRepository;

    public void validateNickname(String nickname) {
        if (!StringUtils.hasText(nickname)) {
            throw new CIllegalArgumentException(ErrorDetail.BLANK_NOT_ALLOWED)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "nickname")
                    .addContext(ErrorContextKeys.OPERATION, "validateNickname")
                    .addContext("reason", "blank");
        }
        if (!isValidNicknameFormat(nickname)) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_NICKNAME_FORMAT)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "nickname")
                    .addContext(ErrorContextKeys.OPERATION, "validateNickname")
                    .addContext("reason", "invalid_format_or_length");
        }
        if (isDuplicateNickname(nickname)) {
            throw new CIllegalArgumentException(ErrorDetail.DUPLICATE_NICKNAME)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "nickname")
                    .addContext(ErrorContextKeys.OPERATION, "validateNickname");
        }
    }

    public void validateEmail(String email) {
        String completeEmail = getCompleteEmail(email);
        if (!StringUtils.hasText(completeEmail)) {
            throw new CIllegalArgumentException(ErrorDetail.BLANK_NOT_ALLOWED)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "email")
                    .addContext(ErrorContextKeys.OPERATION, "validateEmail")
                    .addContext("reason", "blank");
        }
        if (!isValidEmailFormat(completeEmail)) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_EMAIL_FORMAT)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "email")
                    .addContext(ErrorContextKeys.OPERATION, "validateEmail")
                    .addContext("reason", "invalid_format_or_length");
        }
        if (isDuplicateEmail(completeEmail)) {
            throw new CIllegalArgumentException(ErrorDetail.DUPLICATE_EMAIL)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "email")
                    .addContext(ErrorContextKeys.OPERATION, "validateEmail");
        }
    }

    public boolean isNicknameAvailable(String nickname) {
        return isValidNicknameFormat(nickname)
                && !isDuplicateNickname(nickname);
    }

    public boolean isEmailAvailable(String email) {
        String completeEmail = getCompleteEmail(email);
        return isValidEmailFormat(completeEmail)
                && !isDuplicateEmail(completeEmail);
    }

    public boolean isValidNicknameFormat(String nickname) {
        return isValidNicknameLength(nickname) &&
                NICKNAME_PATTERN.matcher(normalize(nickname)).matches();
    }

    public boolean isValidEmailFormat(String email) {
        String completeEmail = getCompleteEmail(email);
        return StringUtils.hasText(completeEmail) &&
                isValidEmailLength(completeEmail) &&
                EMAIL_PATTERN.matcher(normalize(completeEmail)).matches();
    }

    public boolean isDuplicateNickname(String nickname) {
        if (!StringUtils.hasText(nickname)) return false;
        return memberRepository.existsByNickname(normalize(nickname));
    }

    public boolean isDuplicateEmail(String email) {
        if (!StringUtils.hasText(email)) return false;
        String completeEmail = getCompleteEmail(email);
        return memberRepository.existsByEmail(normalize(completeEmail));
    }

    private boolean isValidEmailLength(String email) {
        return email.length() >= EMAIL_MIN_LENGTH && email.length() <= EMAIL_MAX_LENGTH;
    }

    private boolean isValidNicknameLength(String nickname) {
        return nickname.length() >= NICKNAME_MIN_LENGTH && nickname.length() <= NICKNAME_MAX_LENGTH;
    }

    private String normalize(String value) {
        return value.strip().toLowerCase();
    }

    private String getCompleteEmail(String email) {
        if (!StringUtils.hasText(email)) return "";
        String fullEmail = email.strip().toLowerCase();
        if (!fullEmail.contains("@")) {
            fullEmail += EMAIL_DOMAIN;
        }
        log.info("fullEmail: {}", fullEmail);
        return fullEmail;
    }
}
