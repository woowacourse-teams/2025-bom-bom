package news.bombomemail.article.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UnsubscribeUrlExtractorTest {

    private UnsubscribeUrlExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new UnsubscribeUrlExtractor();
    }

    @Test
    void 구독_취소_URL을_성공적으로_추출한다() {
        // given
        String htmlContent = "<html><body><a href=\"https://example.com/unsubscribe?id=123\">Unsubscribe</a></body></html>";

        // when
        String unsubscribeUrl = extractor.extract(htmlContent);

        // then
        assertThat(unsubscribeUrl).isEqualTo("https://example.com/unsubscribe?id=123");
    }

    @Test
    void 구독_취소_URL이_없으면_null을_반환한다() {
        // given
        String htmlContent = "<html><body><a href=\"https://example.com/subscribe\">Subscribe</a></body></html>";

        // when
        String unsubscribeUrl = extractor.extract(htmlContent);

        // then
        assertThat(unsubscribeUrl).isNull();
    }
}
