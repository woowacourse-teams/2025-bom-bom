package news.bombomemail.subscribe.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import news.bombomemail.article.util.UnsubscribeUrlExtractor;
import news.bombomemail.subscribe.domain.UnsubscribePattern;
import news.bombomemail.subscribe.repository.UnsubscribePatternRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import({UnsubscribeUrlExtractor.class, UnsubscribePatternReloadService.class})
class UnsubscribePatternReloaderTest {

    @Autowired
    UnsubscribePatternRepository repository;

    @Autowired
    UnsubscribePatternReloadService reloadService;

    @Autowired
    UnsubscribeUrlExtractor extractor;

    @Test
    void DB_패턴으로_reload후에_새_패턴으로_추출한다() {
        // given
        repository.save(UnsubscribePattern.builder()
                .patternKey("parse.url-keywords")
                .patternValue("cancel,optout")
                .build());
        repository.save(UnsubscribePattern.builder()
                .patternKey("parse.text-keywords")
                .patternValue("cancel,취소,해지")
                .build());

        // when
        reloadService.reload();

        // then
        assertSoftly(softly -> {
            softly.assertThat(extractor.extract("<a href=\"https://example.com/cancel?id=123\">cancel</a>"))
                    .isEqualTo("https://example.com/cancel?id=123");
            softly.assertThat(extractor.extract("<a href=\"https://example.com/optout?id=123\">manage</a>"))
                    .isEqualTo("https://example.com/optout?id=123");
            softly.assertThat(extractor.extract("<a href=\"https://example.com/cancel\">취소</a>"))
                    .isEqualTo("https://example.com/cancel");
            softly.assertThat(extractor.extract("<a href=\"https://example.com/unsubscribe\">unsubscribe</a>"))
                    .isNull();
        });
    }

    @Test
    void DB가_비어있으면_기본값으로_추출한다() {
        // when
        reloadService.reload();

        // then
        assertSoftly(softly -> {
            softly.assertThat(extractor.extract("<a href=\"https://example.com/unsubscribe?id=123\">Unsubscribe</a>"))
                    .isEqualTo("https://example.com/unsubscribe?id=123");
            softly.assertThat(extractor.extract("<a href=\"https://example.com/cancel\">수신거부</a>"))
                    .isEqualTo("https://example.com/cancel");
            softly.assertThat(extractor.extract("<a href=\"https://example.com/page\">cancel</a>"))
                    .isNull();
        });
    }
}
