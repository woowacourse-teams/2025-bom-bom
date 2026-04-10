package news.bombomemail.article.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UnsubscribeUrlExtractorTest {

    @Test
    void URL에_unsubscribe가_있으면_추출한다() {
        String html = "<a href=\"https://example.com/unsubscribe?id=123\">Unsubscribe</a>";
        assertThat(UnsubscribeUrlExtractor.extract(html))
                .isEqualTo("https://example.com/unsubscribe?id=123");
    }

    @Test
    void 앵커_텍스트가_Unsubscribe이면_추출한다() {
        // theSkimm, Korean FE Article
        String html = "<a href=\"https://link.example.com/oc/abc123\">Unsubscribe or Update Your Preferences</a>";
        assertThat(UnsubscribeUrlExtractor.extract(html))
                .isEqualTo("https://link.example.com/oc/abc123");
    }

    @Test
    void span_내부에_Unsubscribe가_있으면_추출한다() {
        // Substack
        String html = "<a href=\"https://substack.com/redirect/2/eyJl...\"><span>Unsubscribe</span></a>";
        assertThat(UnsubscribeUrlExtractor.extract(html))
                .isEqualTo("https://substack.com/redirect/2/eyJl...");
    }

    @Test
    void 앵커_텍스트가_수신거부이면_추출한다() {
        // NHN Cloud, Careet
        String html = "<a href=\"http://mkt.nhncloud.com/u/NDg4...\">수신거부</a>";
        assertThat(UnsubscribeUrlExtractor.extract(html))
                .isEqualTo("http://mkt.nhncloud.com/u/NDg4...");
    }

    @Test
    void 대괄호로_감싼_수신거부도_추출한다() {
        // Careet
        String html = "<a href=\"https://event.stibee.com/v2/click/abc\">[수신거부]</a>";
        assertThat(UnsubscribeUrlExtractor.extract(html))
                .isEqualTo("https://event.stibee.com/v2/click/abc");
    }

    @Test
    void 앵커_텍스트가_Unsubscription이면_추출한다() {
        // NHN Cloud
        String html = "<a href=\"http://mkt.nhncloud.com/u/NDg4...\">Unsubscription)</a>";
        assertThat(UnsubscribeUrlExtractor.extract(html))
                .isEqualTo("http://mkt.nhncloud.com/u/NDg4...");
    }

    @Test
    void 앵커_텍스트가_구독취소이면_추출한다() {
        String html = "<a href=\"https://example.com/cancel\">구독 취소</a>";
        assertThat(UnsubscribeUrlExtractor.extract(html))
                .isEqualTo("https://example.com/cancel");
    }

    @Test
    void 앵커_텍스트가_구독해지이면_추출한다() {
        String html = "<a href=\"https://example.com/cancel\">구독 해지</a>";
        assertThat(UnsubscribeUrlExtractor.extract(html))
                .isEqualTo("https://example.com/cancel");
    }

    @Test
    void 본문_링크에_unsubscribe가_포함되어도_오탐하지_않는다() {
        String html = "<a href=\"https://blog.example.com/email-tips\">Why unsubscribe rates matter for marketers</a>";
        assertThat(UnsubscribeUrlExtractor.extract(html)).isNull();
    }

    @Test
    void 수신거부_URL이_없으면_null을_반환한다() {
        String html = "<a href=\"https://example.com/subscribe\">Subscribe</a>";
        assertThat(UnsubscribeUrlExtractor.extract(html)).isNull();
    }

    @Test
    void null_입력이면_null을_반환한다() {
        assertThat(UnsubscribeUrlExtractor.extract(null)).isNull();
    }
}
