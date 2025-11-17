package me.bombom.api.v1.article.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.enums.Gender;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterDetailRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

@IntegrationTest
class PreviousArticleServiceTest {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2025, 7, 15, 10, 0);

    @Autowired
    private NewsletterRepository newsletterRepository;

    @Autowired
    private NewsletterDetailRepository newsletterDetailRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PreviousArticleService previousArticleService;

    List<Category> categories;
    List<Newsletter> newsletters;
    List<Article> articles;
    Member member;

    @BeforeEach
    public void setup() {
        newsletterRepository.deleteAllInBatch();
        articleRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        member = TestFixture.normalMemberFixture();
        memberRepository.save(member);
        categories = TestFixture.createCategories();
        categoryRepository.saveAll(categories);
        List<NewsletterDetail> newsletterDetails = TestFixture.createNewsletterDetails();
        newsletterDetailRepository.saveAll(newsletterDetails);
        newsletters = TestFixture.createNewslettersWithDetails(categories, newsletterDetails);
        newsletterRepository.saveAll(newsletters);
        articles = TestFixture.createArticles(member, newsletters);
        articleRepository.saveAll(articles);
    }

    @Test
    void 관리자_아티클_정리_테스트() {
        // given
        Member admin = Member.builder()
                .provider("admin")
                .providerId("admin123")
                .email("admin@bombom.news")
                .nickname("봄봄")
                .gender(Gender.MALE)
                .roleId(1L)
                .build();
        memberRepository.save(admin);

        // 테스트용 설정값 오버라이드
        ReflectionTestUtils.setField(previousArticleService, "PREVIOUS_ARTICLE_ADMIN_ID", admin.getId());
        Newsletter newsletter1 = newsletters.getFirst();
        List<Article> adminArticles = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            Article article = TestFixture.createArticle(
                    "관리자 아티클 " + i,
                    admin.getId(),
                    newsletter1.getId(),
                    BASE_TIME.minusDays(i)
            );
            adminArticles.add(article);
        }

        articleRepository.saveAll(adminArticles);

        // when
        int deletedCount = previousArticleService.cleanupOldPreviousArticles();

        // then
        assertSoftly(softly -> {
            softly.assertThat(deletedCount).isEqualTo(2);

            // 뉴스레터1에 10개만 남아있는지 확인
            long newsletter1Count = articleRepository.findAll().stream()
                    .filter(article ->
                            article.getMemberId().equals(admin.getId()) &&
                                    article.getNewsletterId().equals(newsletter1.getId()))
                    .count();
            softly.assertThat(newsletter1Count).isEqualTo(10);

            // 일반 사용자 아티클은 영향받지 않았는지 확인
            long memberCount = articleRepository.findAll().stream()
                    .filter(article -> article.getMemberId().equals(member.getId()))
                    .count();
            softly.assertThat(memberCount).isEqualTo(4);
        });
    }
}
