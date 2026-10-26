package news.bombomemail.article.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * [임시 코드 - 제거 예정] {@link ContentsTextTruncator}와 함께 제거할 것.
 */
class ContentsTextTruncatorTest {

    @Test
    void 상한보다_짧으면_원본_인스턴스를_그대로_반환한다() {
        String text = "짧은 뉴스레터 본문입니다";

        // 호출부가 참조 비교로 잘림 여부를 판단하므로 동일 인스턴스여야 한다
        assertThat(ContentsTextTruncator.truncate(text)).isSameAs(text);
    }

    @Test
    void null이면_null을_반환한다() {
        assertThat(ContentsTextTruncator.truncate(null)).isNull();
    }

    @Test
    void 상한을_넘으면_상한_이하로_자른다() {
        String text = "가".repeat(30_000); // 한글 3바이트 × 30,000 = 90,000바이트

        String truncated = ContentsTextTruncator.truncate(text);

        assertThat(byteLength(truncated)).isLessThanOrEqualTo(ContentsTextTruncator.MAX_BYTES);
        assertThat(truncated).isNotSameAs(text);
    }

    @Test
    void 자른_뒤에도_한글이_깨지지_않는다() {
        // 65,000은 3의 배수가 아니라 한글 경계에 걸친다
        String text = "가".repeat(30_000);

        String truncated = ContentsTextTruncator.truncate(text);

        assertThat(truncated).doesNotContain("�"); // 치환 문자
        assertThat(truncated.chars()).allMatch(c -> c == '가');
    }

    @Test
    void 자른_결과는_원본의_앞부분을_유지한다() {
        String prefix = "뉴스레터 제목과 도입부";
        String text = prefix + "나".repeat(30_000);

        assertThat(ContentsTextTruncator.truncate(text)).startsWith(prefix);
    }

    @Test
    void 상한에_정확히_걸치는_길이는_자르지_않는다() {
        String text = "a".repeat(ContentsTextTruncator.MAX_BYTES); // ASCII 1바이트

        assertThat(ContentsTextTruncator.truncate(text)).isSameAs(text);
    }

    @Test
    void 상한을_한_바이트_넘으면_자른다() {
        String text = "a".repeat(ContentsTextTruncator.MAX_BYTES + 1);

        String truncated = ContentsTextTruncator.truncate(text);

        assertThat(byteLength(truncated)).isEqualTo(ContentsTextTruncator.MAX_BYTES);
    }

    @Test
    void 이모지가_경계에_걸려도_깨지지_않는다() {
        // 이모지는 4바이트. 경계에 2바이트만 걸치도록 ASCII로 길이를 맞춘다
        String padding = "a".repeat(ContentsTextTruncator.MAX_BYTES - 2);
        String text = padding + "🔥🔥";

        String truncated = ContentsTextTruncator.truncate(text);

        assertThat(truncated).doesNotContain("�");
        assertThat(truncated).isEqualTo(padding);
    }

    @Test
    void 잘린_결과는_TEXT_컬럼_한계보다_작다() {
        String text = "가".repeat(30_000);

        String truncated = ContentsTextTruncator.truncate(text);

        // 실제 방어 목적: MySQL TEXT(65,535바이트)에 반드시 들어가야 한다
        assertThat(byteLength(truncated)).isLessThan(65_535);
    }

    @Test
    void byteLength는_한글을_세_바이트로_센다() {
        assertThat(ContentsTextTruncator.byteLength("가")).isEqualTo(3);
        assertThat(ContentsTextTruncator.byteLength("a")).isEqualTo(1);
        assertThat(ContentsTextTruncator.byteLength(null)).isZero();
    }

    private int byteLength(String text) {
        return text.getBytes(StandardCharsets.UTF_8).length;
    }
}
