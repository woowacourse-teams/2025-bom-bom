package me.bombom.api.v1.nativenewsletter.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NativeNewsletterTextUtil {

    private static final int SUMMARY_MAX_LENGTH = 200;
    private static final int WORDS_PER_MINUTE = 200;

    public static String stripTags(String html) {
        return html.replaceAll("<[^>]+>", "").strip();
    }

    public static String summarize(String text) {
        return text.length() <= SUMMARY_MAX_LENGTH ? text : text.substring(0, SUMMARY_MAX_LENGTH);
    }

    public static int calculateReadTime(String text) {
        int wordCount = text.split("\\s+").length;
        return Math.max(1, wordCount / WORDS_PER_MINUTE);
    }
}
