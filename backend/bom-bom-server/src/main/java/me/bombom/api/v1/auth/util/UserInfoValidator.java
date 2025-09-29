package me.bombom.api.v1.auth.util;

import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

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
    private static final String NICKNAME_REGEX_PATTERN = "^(?!.*\\.\\.)([A-Za-z0-9가-힣](?:[A-Za-z0-9가-힣._ ]*[A-Za-z0-9가-힣])?)(?:#\\d+)?$";
    private static final String EMAIL_REGEX_PATTERN = "^[a-zA-Z0-9](?:[a-zA-Z0-9._-]*[a-zA-Z0-9])?@bombom\\.news$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX_PATTERN);
    private static final Pattern NICKNAME_PATTERN = Pattern.compile(NICKNAME_REGEX_PATTERN);

    private final MemberRepository memberRepository;

    public boolean isNicknameAvailable(String nickname) {
        return isValidNicknameFormat(nickname) && !isDuplicateNickname(nickname);
    }

    public boolean isEmailAvailable(String email) {
        return isValidEmailFormat(email) && !isDuplicateEmail(email);
    }

    public boolean isValidNicknameFormat(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return false;
        }
        return NICKNAME_PATTERN.matcher(nickname.strip()).matches();
    }

    public boolean isValidEmailFormat(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.strip().toLowerCase()).matches();
    }

    public boolean isDuplicateNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return false;
        }
        return memberRepository.existsByNickname(nickname.strip().toLowerCase());
    }

    public boolean isDuplicateEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return memberRepository.existsByEmail(email.strip().toLowerCase());
    }
}
