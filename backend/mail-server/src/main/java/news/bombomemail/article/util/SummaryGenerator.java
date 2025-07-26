package news.bombomemail.article.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jsoup.Jsoup;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SummaryGenerator {

    private static final int MAX_LENGTH = 100;

    public static String summarize(String htmlText) {
        if (htmlText == null || htmlText.isBlank()) return "";
        String plain = Jsoup.parse(htmlText).text();
        if (plain.length() <= MAX_LENGTH) return plain;
        return plain.substring(0, MAX_LENGTH) + "...";
    }
}
