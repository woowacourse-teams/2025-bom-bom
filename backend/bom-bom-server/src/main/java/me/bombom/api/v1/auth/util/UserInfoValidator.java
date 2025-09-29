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

    private static final String NICKNAME_REGEX_PATTERN = "^(?!.*\\.\\.)[A-Za-z0-9가-힣][A-Za-z0-9가-힣._ ]*[A-Za-z0-9가-힣]$";
    private static final String EMAIL_REGEX_PATTERN = "^[a-zA-Z0-9](?:[a-zA-Z0-9._-]*[a-zA-Z0-9])?@bombom\\.news$";

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
        Pattern pattern = Pattern.compile(NICKNAME_REGEX_PATTERN);
        return pattern.matcher(nickname.strip()).matches();
    }

    public boolean isValidEmailFormat(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        Pattern pattern = Pattern.compile(EMAIL_REGEX_PATTERN);
        return pattern.matcher(email.strip().toLowerCase()).matches();
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
