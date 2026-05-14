package news.bombomemail.newsletter.domain;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.junit.jupiter.api.Test;

class NewsletterTest {

    @Test
    void source와_status를_지정하지_않으면_기본값을_사용한다() {
        Newsletter newsletter = createNewsletterBuilder().build();

        assertSoftly(softly -> {
            softly.assertThat(newsletter.getSource()).isEqualTo(NewsletterSource.EXTERNAL);
            softly.assertThat(newsletter.getStatus()).isEqualTo(NewsletterPublicationStatus.ACTIVE);
        });
    }

    @Test
    void source와_status를_명시할_수_있다() {
        Newsletter newsletter = createNewsletterBuilder()
                .source(NewsletterSource.MAEIL_MAIL)
                .status(NewsletterPublicationStatus.SUSPENDED)
                .build();

        assertSoftly(softly -> {
            softly.assertThat(newsletter.getSource()).isEqualTo(NewsletterSource.MAEIL_MAIL);
            softly.assertThat(newsletter.getStatus()).isEqualTo(NewsletterPublicationStatus.SUSPENDED);
        });
    }

    private Newsletter.NewsletterBuilder createNewsletterBuilder() {
        return Newsletter.builder()
                .name("테스트뉴스레터")
                .description("설명")
                .imageUrl("이미지")
                .email("test-newsletter@example.com")
                .categoryId(1L)
                .detailId(1L);
    }
}
