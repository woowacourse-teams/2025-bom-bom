package news.bombomemail.article.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * [임시 코드 - 제거 예정]
 *
 * <p>article.contents_text 컬럼이 TEXT(최대 65,535바이트)라서, 본문 평문이 이를 넘기면
 * 저장에 실패한다. 해당 컬럼을 MEDIUMTEXT로 변경하는 마이그레이션 전까지만 쓰는 임시 방어 코드다.
 *
 * <p><b>마이그레이션(TEXT → MEDIUMTEXT) 완료 후 반드시 제거할 것.</b> 제거 대상:
 * <ul>
 *     <li>이 클래스</li>
 *     <li>{@code ContentsTextTruncatorTest}</li>
 *     <li>{@code ArticleService}에서 이 클래스를 호출하는 부분
 *         ({@code truncateContentsText}, {@code safeSubject} 메서드와 import 포함)</li>
 * </ul>
 *
 * <p>잘린 아티클은 {@code ArticleService}에서 WARN 로그로 남긴다. 마이그레이션 이후
 * 그 로그를 기준으로 원본을 재처리해야 온전한 본문으로 복구된다 — 컬럼만 넓힌다고
 * 이미 잘려 저장된 데이터가 돌아오지는 않는다.
 *
 * <p>recent_article.contents_text는 이미 MEDIUMTEXT이므로 이 클래스를 적용하지 않는다.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ContentsTextTruncator {

    /**
     * 자를 기준 크기. 글자 수가 아니라 바이트 수다.
     *
     * <p>MySQL TEXT의 한계는 65,535바이트지만, 한계치에 딱 맞추면 여유가 없어
     * 저장 실패를 확실히 막지 못한다. 마진을 두고 65,000바이트로 자른다
     * (utf8mb4 기준 한글 약 180자 차이라 검색 품질에는 영향이 없다).
     */
    public static final int MAX_BYTES = 65_000;

    /**
     * UTF-8 기준 {@link #MAX_BYTES} 이하가 되도록 뒤를 자른다.
     * 경계에서 멀티바이트 문자가 쪼개지지 않도록 문자 단위로 끊는다.
     *
     * <p>자르지 않은 경우 <b>원본 인스턴스를 그대로</b> 반환하므로, 호출부에서
     * 참조 비교({@code result != original})로 잘림 여부를 알 수 있다.
     *
     * @return 상한 이하면 원본 그대로, 넘으면 잘린 문자열
     */
    public static String truncate(String contentsText) {
        if (contentsText == null) {
            return null;
        }
        byte[] bytes = contentsText.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= MAX_BYTES) {
            return contentsText;
        }
        return decodeIgnoringSplitCharacter(bytes);
    }

    /**
     * UTF-8 인코딩 기준 바이트 길이. 잘림 로그에 단위를 맞춰 찍기 위해 쓴다.
     */
    public static int byteLength(String contentsText) {
        if (contentsText == null) {
            return 0;
        }
        return contentsText.getBytes(StandardCharsets.UTF_8).length;
    }

    /**
     * 상한 지점에서 잘린 바이트 배열을 디코딩한다. 경계에 멀티바이트 문자가 걸쳐
     * 불완전한 바이트가 남으면 치환 문자를 넣지 않고 버린다.
     */
    private static String decodeIgnoringSplitCharacter(byte[] bytes) {
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.IGNORE)
                .onUnmappableCharacter(CodingErrorAction.IGNORE);
        ByteBuffer truncated = ByteBuffer.wrap(bytes, 0, MAX_BYTES);
        CharBuffer decoded = CharBuffer.allocate(MAX_BYTES);

        decoder.decode(truncated, decoded, true);
        decoder.flush(decoded);
        decoded.flip();

        return decoded.toString();
    }
}
