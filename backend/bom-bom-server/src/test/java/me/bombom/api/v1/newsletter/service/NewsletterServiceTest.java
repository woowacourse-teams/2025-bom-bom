package me.bombom.api.v1.newsletter.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.newsletter.dto.NewsletterResponse;
import me.bombom.api.v1.newsletter.repository.NewsletterDetailRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(NewsletterService.class)
class NewsletterServiceTest {

    @Autowired
    private NewsletterService newsletterService;

    @Autowired
    private NewsletterRepository newsletterRepository;

    @Autowired
    private NewsletterDetailRepository newsletterDetailRepository;

    List<Newsletter> newsletters;

    @BeforeEach
    public void setup() {
        List<NewsletterDetail> newsletterDetails = List.of(
            NewsletterDetail.builder()
                    .mainPageUrl("https://news1.com")
                    .subscribeUrl("https://news1.com/subscribe")
                    .issueCycle("매일")
                    .subscribeCount(1000)
                    .build(),
            NewsletterDetail.builder()
                    .mainPageUrl("https://ittimes.com")
                    .subscribeUrl("https://ittimes.com/subscribe")
                    .issueCycle("매주 월요일")
                    .subscribeCount(850)
                    .build(),
            NewsletterDetail.builder()
                    .mainPageUrl("https://biz.com")
                    .subscribeUrl("https://biz.com/subscribe")
                    .issueCycle("격주 화요일")
                    .subscribeCount(600)
                    .build()
            );
        newsletterDetailRepository.saveAll(newsletterDetails);
        newsletters = List.of(
            Newsletter.builder()
                    .name("뉴스픽")
                    .description("뉴스픽 요약 뉴스")
                    .imageUrl("https://cdn.bombom.me/img1.png")
                    .email("news@newspick.com")
                    .categoryId(1L)
                    .detailId(1L)
                    .build(),
            Newsletter.builder()
                    .name("IT타임즈")
                    .description("IT 업계 트렌드")
                    .imageUrl("https://cdn.bombom.me/img2.png")
                    .email("editor@ittimes.io")
                    .categoryId(2L)
                    .detailId(2L)
                    .build(),
            Newsletter.builder()
                    .name("비즈레터")
                    .description("비즈니스 뉴스 큐레이션")
                    .imageUrl("https://cdn.bombom.me/img3.png")
                    .email("biz@biz.com")
                    .categoryId(3L)
                    .detailId(3L)
                    .build()
        );
        newsletterRepository.saveAll(newsletters);
    }

    @Test
    void 뉴스레터를_모두_조회할_수_있다() {
        //when
        List<NewsletterResponse> result = newsletterService.getNewsletters();

        //then
        assertThat(result.size()).isEqualTo(newsletters.size());
    }
}
