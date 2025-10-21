package me.bombom.api.v1.auth.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UniqueUserInfoGeneratorTest {

    @InjectMocks
    private UniqueUserInfoGenerator uniqueUserInfoGenerator;

    @Mock
    private UserInfoValidator userInfoValidator;

    @Test
    void 닉네임이_이미_사용_가능할_경우_원래_닉네임을_반환한다() {
        // given
        String nickname = "테스트닉네임";
        when(userInfoValidator.isNicknameAvailable(nickname.toLowerCase())).thenReturn(true);

        // when
        String result = uniqueUserInfoGenerator.getUniqueNickname(nickname);

        // then
        System.out.println(result);
        assertThat(result).isEqualTo(nickname);
    }

    @Test
    void 닉네임이_중복될_경우_완전히_새로운_랜덤_닉네임을_생성한다() {
        // given
        String nickname = "테스트 닉네임";
        when(userInfoValidator.isNicknameAvailable(nickname.toLowerCase())).thenReturn(false);
        when(userInfoValidator.isDuplicateNickname(anyString())).thenReturn(false);

        // when
        String result = uniqueUserInfoGenerator.getUniqueNickname(nickname);

        // then
        assertThat(result).isNotEqualTo(nickname);
    }

    @Test
    void 닉네임이_형식을_만족하지_않을_경우_완전히_새로운_랜덤_닉네임을_생성한다() {
        // given
        String nickname = "(테스트닉네임)";
        when(userInfoValidator.isNicknameAvailable(nickname.toLowerCase())).thenReturn(false);
        when(userInfoValidator.isDuplicateNickname(anyString())).thenReturn(false);

        // when
        String result = uniqueUserInfoGenerator.getUniqueNickname(nickname);

        // then
        assertThat(result).isNotEqualTo(nickname);
    }

    @Test
    void 닉네임이_null일_경우_랜덤_형용사와_명사를_조합한_닉네임을_생성한다() {
        // given
        String nickname = null;
        when(userInfoValidator.isDuplicateNickname(anyString())).thenReturn(false);

        // when
        String result = uniqueUserInfoGenerator.getUniqueNickname(nickname);

        // then
        assertThat(result).contains(UniqueUserInfoGenerator.NICKNAME_RANDOM_DELIMITER);
    }

    @Test
    void 닉네임이_공백일_경우_랜덤_형용사와_명사를_조합한_닉네임을_생성한다() {
        // given
        String nickname = " ";
        when(userInfoValidator.isDuplicateNickname(anyString())).thenReturn(false);

        // when
        String result = uniqueUserInfoGenerator.getUniqueNickname(nickname);

        // then
        assertThat(result).contains(UniqueUserInfoGenerator.NICKNAME_RANDOM_DELIMITER);
    }
    
    @Test
    void 생성된_닉네임도_중복될_경우_중복되지_않을_때까지_새로운_닉네임을_생성한다() {
        // given
        String nickname = "테스트닉네임";
        when(userInfoValidator.isNicknameAvailable(nickname.toLowerCase())).thenReturn(false);
        // 첫 번째 생성된 닉네임은 중복, 두 번째는 통과
        when(userInfoValidator.isDuplicateNickname(anyString())).thenReturn(true).thenReturn(false);

        // when
        String result = uniqueUserInfoGenerator.getUniqueNickname(nickname);

        // then
        assertThat(result).isNotEqualTo(nickname);
    }

    @Test
    void 닉네임_완전_랜덤_생성_여러_번_호출시_다른_값이_생성된다() {
        // given
        String nickname = null;
        when(userInfoValidator.isDuplicateNickname(anyString())).thenReturn(false);

        // when
        String result1 = uniqueUserInfoGenerator.getUniqueNickname(nickname);
        String result2 = uniqueUserInfoGenerator.getUniqueNickname(nickname);
        String result3 = uniqueUserInfoGenerator.getUniqueNickname(nickname);

        // then
        assertSoftly(
                softly -> {
                    assertThat(result1).isNotEqualTo(result2);
                    assertThat(result2).isNotEqualTo(result3);
                    assertThat(result1).isNotEqualTo(result3);
                }
        );
    }

    @Test
    void 이메일_로컬_파트가_사용_가능할_경우_원래_로컬_파트를_반환한다() {
        // given
        String email = "test.email";
        when(userInfoValidator.isEmailAvailable(email.toLowerCase())).thenReturn(true);

        // when
        String result = uniqueUserInfoGenerator.getUniqueEmailLocalPart(email);

        // then
        System.out.println(result);
        assertThat(result).isEqualTo(email);
    }

    @Test
    void 이메일_로컬_파트가_중복될_경우_구분자와_랜덤_숫자를_붙인_새로운_로컬_파트를_생성한다() {
        // given
        String email = "test.email";
        when(userInfoValidator.isEmailAvailable(email.toLowerCase())).thenReturn(false);

        // when
        String result = uniqueUserInfoGenerator.getUniqueEmailLocalPart(email);

        // then
        System.out.println(result);
        assertThat(result).startsWith(email + UniqueUserInfoGenerator.EMAIL_RANDOM_DELIMITER);
    }

    @Test
    void 이메일이_null일_경우_UUID_8자리_랜덤_로컬_파트를_생성한다() {
        // given
        String email = null;

        // when
        String result = uniqueUserInfoGenerator.getUniqueEmailLocalPart(email);

        // then
        assertThat(result).matches("[a-f0-9]{8}"); // 16진수 8자리
    }

    @Test
    void 이메일이_빈_문자열일_경우_UUID_8자리_랜덤_로컬_파트를_생성한다() {
        // given
        String email = "";

        // when
        String result = uniqueUserInfoGenerator.getUniqueEmailLocalPart(email);

        // then
        assertThat(result).matches("[a-f0-9]{8}"); // 16진수 8자리
    }

    @Test
    void 이메일이_공백일_경우_UUID_8자리_랜덤_로컬_파트를_생성한다() {
        // given
        String email = "   ";

        // when
        String result = uniqueUserInfoGenerator.getUniqueEmailLocalPart(email);

        // then
        assertThat(result).matches("[a-f0-9]{8}"); // 16진수 8자리
    }

    @Test
    void 이메일_로컬_파트_완전_랜덤_생성_여러_번_호출시_다른_값이_생성된다() {
        // given
        String email = null;

        // when
        String result1 = uniqueUserInfoGenerator.getUniqueEmailLocalPart(email);
        String result2 = uniqueUserInfoGenerator.getUniqueEmailLocalPart(email);
        String result3 = uniqueUserInfoGenerator.getUniqueEmailLocalPart(email);

        // then
        assertSoftly(
                softly -> {
                    assertThat(result1).isNotEqualTo(result2);
                    assertThat(result2).isNotEqualTo(result3);
                    assertThat(result1).isNotEqualTo(result3);
                }
        );
    }
}
