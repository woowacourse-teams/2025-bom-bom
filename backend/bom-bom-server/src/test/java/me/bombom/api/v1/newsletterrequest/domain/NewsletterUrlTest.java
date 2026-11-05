package me.bombom.api.v1.newsletterrequest.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

class NewsletterUrlTest {

    @ParameterizedTest
    @CsvSource({
            "https://www.Example.com/, example.com",
            "example.com/news?utm=1#top, example.com/news",
            "http://example.com/a/, example.com/a",
            "'  https://weeklydev.stibee.com  ', weeklydev.stibee.com"
    })
    void URL을_정규화한다(String rawUrl, String expected) {
        NewsletterUrl newsletterUrl = NewsletterUrl.from(rawUrl);

        assertThat(newsletterUrl.getNormalized()).isEqualTo(expected);
        assertThat(newsletterUrl.getOriginal()).isEqualTo(rawUrl.strip());
    }

    @ParameterizedTest
    @ValueSource(strings = {"notaurl", " ", "ftp://example.com", "https://"})
    void 형식이_잘못된_URL이면_예외가_발생한다(String rawUrl) {
        assertThatThrownBy(() -> NewsletterUrl.from(rawUrl))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 길이가_512자를_넘으면_예외가_발생한다() {
        String rawUrl = "https://example.com/" + "a".repeat(493);

        assertThatThrownBy(() -> NewsletterUrl.from(rawUrl))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 정규화할_수_없는_URL이면_null을_반환한다() {
        assertThat(NewsletterUrl.normalize("notaurl")).isNull();
        assertThat(NewsletterUrl.normalize(null)).isNull();
    }
}
