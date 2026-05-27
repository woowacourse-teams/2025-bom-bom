package me.bombom.api.v1.article.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;

import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.event.MarkAsReadEvent;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterDetailRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.api.v1.reading.repository.MemberReadTokenBucketRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

@IntegrationTest
@RecordApplicationEvents
class ArticleServiceRateLimitTransactionTest {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2025, 7, 15, 10, 0);

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private NewsletterRepository newsletterRepository;

    @Autowired
    private NewsletterDetailRepository newsletterDetailRepository;

    @MockitoBean
    private MemberReadTokenBucketRepository memberReadTokenBucketRepository;

    @Autowired
    private ApplicationEvents applicationEvents;

    private Member member;
    private Article article;

    @BeforeEach
    void setUp() {
        articleRepository.deleteAllInBatch();
        newsletterRepository.deleteAllInBatch();
        newsletterDetailRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        member = memberRepository.save(TestFixture.normalMemberFixture());

        Category category = categoryRepository.save(TestFixture.createCategory());
        NewsletterDetail newsletterDetail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(false));
        Newsletter newsletter = newsletterRepository.save(
                TestFixture.createNewsletter("테스트 뉴스레터", "test@example.com", category.getId(), newsletterDetail.getId())
        );
        article = articleRepository.save(TestFixture.createArticle("제목", member.getId(), newsletter.getId(), BASE_TIME));
    }

    @Test
    void 읽기_토큰_소비_중_일시적_DB_예외가_발생해도_읽음_처리는_커밋된다() {
        // given
        doThrow(new TransientDataAccessResourceException("DB 일시 장애"))
                .when(memberReadTokenBucketRepository)
                .insertIfAbsent(anyLong(), anyInt(), any(LocalDateTime.class));

        // when & then
        assertThatCode(() -> articleService.markAsRead(article.getId(), member))
                .doesNotThrowAnyException();

        List<MarkAsReadEvent> events = applicationEvents.stream(MarkAsReadEvent.class).toList();
        MarkAsReadEvent event = events.getFirst();

        assertSoftly(softly -> {
            softly.assertThat(articleRepository.findById(article.getId())).hasValueSatisfying(savedArticle ->
                    softly.assertThat(savedArticle.isRead()).isTrue()
            );
            softly.assertThat(event.countable()).isTrue();
        });
    }
}
