package news.bombomemail.article.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UnsubscribeUrlExtractor {

    private static final Pattern UNSUBSCRIBE_URL_PATTERN = Pattern.compile("href=\"([^\"]*unsubscribe[^\"]*)\"", Pattern.CASE_INSENSITIVE);

    public static String extract(String articleContents) {
        if (articleContents == null) {
            return null;
        }
        Matcher matcher = UNSUBSCRIBE_URL_PATTERN.matcher(articleContents);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
