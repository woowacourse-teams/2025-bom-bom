package news.bombomemail.article.util;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class UnsubscribeUrlExtractor {

    private static final Pattern ANCHOR_PATTERN = Pattern.compile(
            "<a\\s[^>]*href=\"([^\"]+)\"[^>]*>(.*?)</a>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private static final Pattern INNER_TAG_PATTERN = Pattern.compile("<[^>]+>");

    private final AtomicReference<Pattern> urlPattern = new AtomicReference<>();
    private final AtomicReference<Pattern> textPattern = new AtomicReference<>();
    private final AtomicReference<Pattern> adjacentTextPattern = new AtomicReference<>();

    public void reload(List<String> urlKeywords, List<String> textKeywords) {
        urlPattern.set(Pattern.compile(buildUrlRegex(urlKeywords), Pattern.CASE_INSENSITIVE));
        String textKeywordGroup = String.join("|", textKeywords);
        textPattern.set(Pattern.compile("^[\\[(\\s]*(" + textKeywordGroup + ")", Pattern.CASE_INSENSITIVE));
        adjacentTextPattern.set(
                Pattern.compile(
                        "(?:" + textKeywordGroup + ")\\s{0,3}<a\\s[^>]*href=\"([^\"]+)\"",
                        Pattern.CASE_INSENSITIVE
                )
        );
    }

    public String extract(String articleContents) {
        if (!StringUtils.hasText(articleContents)) {
            return null;
        }

        Matcher urlMatcher = urlPattern.get().matcher(articleContents);
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
            if (textPattern.get().matcher(text).find()) {
                return href;
            }
        }

        Matcher adjacentMatcher = adjacentTextPattern.get().matcher(articleContents);
        if (adjacentMatcher.find()) {
            return adjacentMatcher.group(1);
        }

        return null;
    }

    private static String buildUrlRegex(List<String> urlKeywords) {
        String joinedKeywords = urlKeywords.stream()
                .map(Pattern::quote)
                .reduce((left, right) -> left + "|" + right)
                .orElseThrow();
        return "href=\"([^\"]*(?:" + joinedKeywords + ")[^\"]*)\"";
    }

}
