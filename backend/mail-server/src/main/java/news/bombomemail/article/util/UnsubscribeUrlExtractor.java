package news.bombomemail.article.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public final class UnsubscribeUrlExtractor {

    private static final Pattern UNSUBSCRIBE_URL_PATTERN = Pattern.compile("href=\"([^\"]*unsubscribe[^\"]*)\"", Pattern.CASE_INSENSITIVE);

    private UnsubscribeUrlExtractor() {}

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
