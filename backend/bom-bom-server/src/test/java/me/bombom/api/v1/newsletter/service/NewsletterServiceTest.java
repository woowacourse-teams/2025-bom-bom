package me.bombom.api.v1.newsletter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.dto.NewsletterResponse;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
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

    @Autowired
    private CategoryRepository categoryRepository;

    private List<Newsletter> newsletters;

    @BeforeEach
    public void setup() {
        newsletterDetailRepository.saveAll(TestFixture.createNewsletterDetails());
        List<Category> categories = TestFixture.createCategories();
        categoryRepository.saveAll(categories);
        newsletters = TestFixture.createNewsletters(categories);
        newsletterRepository.saveAll(newsletters);
    }

    @Test
    void 뉴스레터를_모두_조회할_수_있다() {
        //when
        List<NewsletterResponse> result = newsletterService.getNewsletters();

        //then
        assertAll(
                () -> assertThat(result.size()).isEqualTo(newsletters.size()),
                () -> assertThat(result)
                    .extracting("newsletterId")
                    .containsExactlyInAnyOrder(
                            newsletters.get(0).getId(),
                            newsletters.get(1).getId(),
                            newsletters.get(2).getId()
                    )
        );
    }
}
