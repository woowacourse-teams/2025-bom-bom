package news.bombomemail.article.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UnsubscribeUrlExtractor {

    // 1단계: href URL 자체에 "unsubscribe"가 포함된 경우
    private static final Pattern UNSUBSCRIBE_URL_PATTERN = Pattern.compile(
            "href=\"([^\"]*unsubscribe[^\"]*)\"",
            Pattern.CASE_INSENSITIVE
    );

    // 2단계: <a href="...">...</a> 블록 전체를 추출 (href와 내부 HTML을 함께 캡처)
    private static final Pattern ANCHOR_PATTERN = Pattern.compile(
            "<a\\s[^>]*href=\"([^\"]+)\"[^>]*>(.*?)</a>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    // 앵커 내부의 <span> 등 중첩 태그를 제거해 순수 텍스트만 남기기 위한 패턴
    private static final Pattern INNER_TAG_PATTERN = Pattern.compile("<[^>]+>");

    // 앵커 텍스트가 수신거부 키워드로 시작하는지 확인. 앞에 '[', '(' 등 브라켓이 있어도 허용 (예: "[수신거부]")
    private static final Pattern UNSUBSCRIBE_TEXT_PATTERN = Pattern.compile(
            "^[\\[(\\s]*(unsubscribe|unsubscription|수신\\s*거부|구독\\s*취소|구독\\s*해지)",
            Pattern.CASE_INSENSITIVE
    );

    public static String extract(String articleContents) {
        if (StringUtils.hasText(articleContents)) {
            return null;
        }

        Matcher urlMatcher = UNSUBSCRIBE_URL_PATTERN.matcher(articleContents);
        if (urlMatcher.find()) {
            return urlMatcher.group(1);
        }

        Matcher anchorMatcher = ANCHOR_PATTERN.matcher(articleContents);
        while (anchorMatcher.find()) {
            String href = anchorMatcher.group(1);
            String anchorBody = anchorMatcher.group(2);
            if (href == null || anchorBody == null) {
                continue;
            }
            String text = INNER_TAG_PATTERN.matcher(anchorBody).replaceAll("").strip();
            if (UNSUBSCRIBE_TEXT_PATTERN.matcher(text).find()) {
                return href;
            }
        }

        return null;
    }
}
