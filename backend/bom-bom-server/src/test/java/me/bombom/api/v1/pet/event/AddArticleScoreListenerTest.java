package me.bombom.api.v1.pet.event;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.api.v1.pet.domain.Pet;
import me.bombom.api.v1.pet.domain.Stage;
import me.bombom.api.v1.pet.repository.PetRepository;
import me.bombom.api.v1.pet.repository.StageRepository;
import me.bombom.api.v1.reading.domain.ContinueReading;
import me.bombom.api.v1.reading.repository.ContinueReadingRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AddArticleScoreListenerTest {

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NewsletterRepository newsletterRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private StageRepository stageRepository;

    @Autowired
    private ContinueReadingRepository continueReadingRepository;

    private Member member;
    private Pet pet;
    private Stage stage;
    private Category category;
    private Newsletter newsletter;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(TestFixture.normalMemberFixture());
        category = categoryRepository.save(TestFixture.createCategories().getFirst());
        newsletter = newsletterRepository.save(
                TestFixture.createNewsletter("뉴스레터", "test@email.com", category.getId())
        );
        stage = stageRepository.save(TestFixture.createStage(1, 50));
        pet = petRepository.save(TestFixture.createPet(member, stage.getId()));
    }

    @Test
    void 연속_읽기_보너스_점수와_아티클_점수를_받을_수_있는_경우() {
        // given
        articleRepository.saveAll(List.of(
                        createArticle(member.getId(), true, newsletter.getId(), LocalDateTime.now()),
                        createArticle(member.getId(), true, newsletter.getId(), LocalDateTime.now()),
                        createArticle(member.getId(), true, newsletter.getId(), LocalDateTime.now()),
                        createArticle(member.getId(), false, newsletter.getId(), LocalDateTime.now())
                )
        );
        continueReadingRepository.save(
                ContinueReading.builder()
                        .memberId(member.getId())
                        .dayCount(7)
                        .build()
        );

        // when
        publisher.publishEvent(new AddArticleScoreEvent(member));
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        pet = petRepository.findById(pet.getId()).orElseThrow();
        Assertions.assertThat(pet.getCurrentScore()).isEqualTo(15);
    }

    @Test
    void 아티클_점수만_받을_수_있는_경우() {
        // given
        articleRepository.saveAll(List.of(
                        createArticle(member.getId(), true, newsletter.getId(), LocalDateTime.now()),
                        createArticle(member.getId(), true, newsletter.getId(), LocalDateTime.now()),
                        createArticle(member.getId(), true, newsletter.getId(), LocalDateTime.now()),
                        createArticle(member.getId(), false, newsletter.getId(), LocalDateTime.now())
                )
        );
        continueReadingRepository.save(
                ContinueReading.builder()
                        .memberId(member.getId())
                        .dayCount(6)
                        .build()
        );

        // when
        publisher.publishEvent(new AddArticleScoreEvent(member));
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        pet = petRepository.findById(pet.getId()).orElseThrow();
        Assertions.assertThat(pet.getCurrentScore()).isEqualTo(10);
    }

    @Test
    void 아티클_점수를_받을_수_없는_경우() {
        // given
        articleRepository.saveAll(List.of(
                        createArticle(member.getId(), true, newsletter.getId(), LocalDateTime.now()),
                        createArticle(member.getId(), true, newsletter.getId(), LocalDateTime.now()),
                        createArticle(member.getId(), true, newsletter.getId(), LocalDateTime.now()),
                        createArticle(member.getId(), true, newsletter.getId(), LocalDateTime.now())
                )
        );

        // when
        publisher.publishEvent(new AddArticleScoreEvent(member));
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        pet = petRepository.findById(pet.getId()).orElseThrow();
        Assertions.assertThat(pet.getCurrentScore()).isEqualTo(0);
    }

    private Article createArticle(Long memberId, boolean isRead, Long newsletterId, LocalDateTime arrivedTime) {
        return Article.builder()
                .title("title")
                .contents("<h1>아티클</h1>")
                .thumbnailUrl("https://example.com/images/thumb.png")
                .expectedReadTime(5)
                .contentsSummary("요약")
                .isRead(isRead)
                .memberId(memberId)
                .newsletterId(newsletterId)
                .arrivedDateTime(arrivedTime)
                .build();
    }
}
