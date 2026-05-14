package news.bombomemail.article.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jsoup.Jsoup;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReadingTimeCalculator {

    private static final int WORDS_PER_MINUTE = 200;

    public static int calculate(String htmlText) {
        if (htmlText == null || htmlText.isBlank()) return 0;
        String plain = Jsoup.parse(htmlText)
                .text()
                .strip();
        int count = plain.split("\\s+")
                .length;
        return Math.max((int) Math.ceil((double) count / WORDS_PER_MINUTE), 1);
    }
}
