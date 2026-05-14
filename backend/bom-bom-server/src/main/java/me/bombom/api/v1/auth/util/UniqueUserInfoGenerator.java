package me.bombom.api.v1.auth.util;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.member.util.UserInfoValidator;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    private static final int EMAIL_MAX_LENGTH = 50; // 컬럼과 일치 (전체 길이)
    private static final String[] RANDOM_NICKNAME_ADJECTIVES = {
        "행복한","즐거운","밝은","따뜻한","귀여운","시원한","새로운","친절한","용감한","강한",
                "똑똑한","현명한","지혜로운","총명한","영리한","대담한","차분한","평온한","부지런한","성실한",
                "순수한","맑은","정직한","솔직한","순진한","다정한","상냥한","온화한","따사로운","열정적인",
                "활기찬","생기있는","에너지넘치는","용의주도한","대범한","강렬한","단호한","차가운","신비로운","호기심많은",
                "창의적인","독창적인","유쾌한","재미있는","익살스러운","엉뚱한","유머러스한","엉클어진","도전적인","끈기있는",
                "성공적인","행운의","풍요로운","풍성한","넉넉한","자유로운","독립적인","단단한","견고한","튼튼한",
                "명랑한","활발한","쾌활한","활짝웃는","열린","긍정적인","낙천적인","유연한","부드러운","온유한",
                "따뜻미소짓는","용맹한","기운찬","활짝핀","꽃같은","빛나는","눈부신","반짝이는","찬란한","화려한",
                "차분미소짓는","겸손한","온순한","성숙한","이상적인","현실적인","섬세한","꼼꼼한","철저한","세심한",
                "날카로운","예리한","날렵한","민첩한","빠른","신속한","재빠른","순발력있는","끈끈한","든든한"
    };
    private static final String[] RANDOM_NICKNAME_NOUNS = {
            "호랑이","사자","늑대","여우","곰","펭귄","토끼","강아지","고양이","원숭이",
            "기린","코끼리","얼룩말","하마","코뿔소","치타","표범","판다","고래","상어",
            "돌고래","물개","바다사자","해마","문어","오징어","게","가재","독수리","매",
            "참새","비둘기","까치","까마귀","앵무새","공작","타조","펠리컨","백조","두루미",
            "개구리","두꺼비","도마뱀","거북이","뱀","악어","카멜레온","고슴도치","너구리","다람쥐",
            "햄스터","기니피그","사슴","노루","엘크","순록","바이슨","들소","양","염소",
            "닭","병아리","오리","거위","칠면조","공룡","티라노","트리케라톱스","브라키오","스테고",
            "벌","나비","잠자리","메뚜기","사마귀","개미","딱정벌레","풍뎅이","무당벌레","거미",
            "해바라기","장미","튤립","코스모스","국화","벚꽃","벼","나무","소나무","대나무",
            "별","달","태양","은하","행성","우주","돌","산","바다","강"
    };

    private final UserInfoValidator userInfoValidator;

    public String getUniqueNickname(String nickname) {
        if (StringUtils.hasText(nickname) && userInfoValidator.isNicknameAvailable(nickname.strip().toLowerCase())) {
            return nickname.strip();
        }

        String uniqueNickname = generateUniqueNickname();
        while (userInfoValidator.isDuplicateNickname(uniqueNickname)) {
            uniqueNickname = generateUniqueNickname();
        }
        return uniqueNickname;
    }

    public String getUniqueEmailLocalPart(String email) {
        if (!StringUtils.hasText(email)) {
            return generateRandomEmailLocalPart();
        }
        
        String normalizedEmailLocalPart = email.strip().toLowerCase();
        String localPart = extractEmailLocalPart(normalizedEmailLocalPart);

        if (userInfoValidator.isEmailAvailable(normalizedEmailLocalPart)) {
            return localPart;
        }
        return generateUniqueEmailLocalPart(localPart);
    }

    private String generateUniqueNickname() {
        String uniqueNickname = generateRandomNickname() + NICKNAME_RANDOM_DELIMITER + getRandomValue();
        log.debug("고유 닉네임 생성: {}", uniqueNickname);
        return uniqueNickname;
    }

    private String generateRandomNickname() {
        String adjective = RANDOM_NICKNAME_ADJECTIVES[ThreadLocalRandom.current().nextInt(RANDOM_NICKNAME_ADJECTIVES.length)];
        String noun = RANDOM_NICKNAME_NOUNS[ThreadLocalRandom.current().nextInt(RANDOM_NICKNAME_NOUNS.length)];

        String randomNickname = adjective + noun;
        log.debug("랜덤 닉네임 생성 - 생성: {}", randomNickname);
        return randomNickname;
    }

    private String generateRandomEmailLocalPart() {
        String randomEmailLocalPart = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8);
        log.debug("완전 랜덤 이메일 로컬파트 생성 - 생성: {}", randomEmailLocalPart);
        return randomEmailLocalPart;
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

    private String trimEmailLocalPartForSuffix(String baseLocalPart, String random) {
        // (이메일 최대 길이 - 도메인 길이 - 랜덤값 추가로 붙는 길이)보다 사용자의 이메일이 길면 자름
        int localPartMaxLength = EMAIL_MAX_LENGTH - EMAIL_DOMAIN.length();
        int availableLength = localPartMaxLength - EMAIL_RANDOM_DELIMITER.length() - random.length();
        return baseLocalPart.length() > availableLength && availableLength > 0
                ? baseLocalPart.substring(0, availableLength)
                : baseLocalPart;
    }
}
