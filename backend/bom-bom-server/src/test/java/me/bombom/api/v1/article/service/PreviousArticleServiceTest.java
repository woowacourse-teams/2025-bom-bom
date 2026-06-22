package me.bombom.api.v1.article.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.domain.PreviousArticle;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.article.repository.PreviousArticleRepository;
import me.bombom.api.v1.common.exception.CServerErrorException;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.domain.Role;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.member.repository.RoleRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterDetailRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
    private PreviousArticleRepository previousArticleRepository;

    @Autowired
    private PreviousArticleService previousArticleService;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void 자동_이동된_지난_아티클은_뉴스레터별_최신_10개만_남기고_정리한다() {
        // given
        Newsletter newsletter = savePreviousArticleNewsletter("지난아티클정리");

        saveAutoMovedPreviousArticles(newsletter.getId(), 12);
        saveFixedPreviousArticles(newsletter.getId(), 3);

        // when
        int deletedCount = previousArticleService.cleanupOldPreviousArticles();

        // then
        assertSoftly(softly -> {
            softly.assertThat(deletedCount).isEqualTo(2);
            softly.assertThat(countPreviousArticles(newsletter.getId(), false)).isEqualTo(10);
            softly.assertThat(countPreviousArticles(newsletter.getId(), true)).isEqualTo(3);
        });
    }

    @Test
    void 아카이브_권한_관리자의_아티클을_지난_아티클로_이동한다() {
        // given
        Member admin = saveArchiveAdmin();
        Newsletter newsletter = savePreviousArticleNewsletter("관리자아티클이동");
        saveAdminArticles(admin, newsletter, 5);
        long initialCount = previousArticleRepository.count();

        // when
        previousArticleService.moveAdminArticles();

        // then
        long finalCount = previousArticleRepository.count();
        assertThat(finalCount).isGreaterThan(initialCount);
    }

    @Test
    void 아카이브_권한_관리자가_없으면_아티클_이동에_실패한다() {
        assertThatThrownBy(() -> previousArticleService.moveAdminArticles())
                .isInstanceOf(CServerErrorException.class);
    }

    private Newsletter savePreviousArticleNewsletter(String name) {
        Category category = categoryRepository.save(Category.builder()
                .name(name + "카테고리")
                .build());
        NewsletterDetail detail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(true));
        Newsletter newsletter = TestFixture.createNewsletter(
                name,
                uniqueEmail("previous"),
                category.getId(),
                detail.getId()
        );
        return newsletterRepository.save(newsletter);
    }

    private Member saveArchiveAdmin() {
        Role archiveRole = roleRepository.save(Role.builder().authority("ARCHIVE").build());
        Member admin = TestFixture.createMemberWithRole("지난아티클관리자", uniqueValue("archive"), archiveRole.getId());
        return memberRepository.save(admin);
    }

    private void saveFixedPreviousArticles(Long newsletterId, int count) {
        List<PreviousArticle> articles = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            articles.add(TestFixture.createPreviousArticle(
                    "고정 아티클 " + i,
                    newsletterId,
                    BASE_TIME.minusDays(10 + i)
            ));
        }
        previousArticleRepository.saveAll(articles);
    }

    private void saveAutoMovedPreviousArticles(Long newsletterId, int count) {
        List<PreviousArticle> articles = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            articles.add(PreviousArticle.builder()
                    .title("자동 이동 아티클 " + i)
                    .contents("<h1>내용</h1>")
                    .contentsSummary("요약")
                    .expectedReadTime(5)
                    .newsletterId(newsletterId)
                    .arrivedDateTime(BASE_TIME.minusDays(i))
                    .isFixed(false)
                    .build());
        }
        previousArticleRepository.saveAll(articles);
    }

    private void saveAdminArticles(Member admin, Newsletter newsletter, int count) {
        List<Article> articles = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            articles.add(TestFixture.createArticle(
                    "관리자 아티클 " + i,
                    admin.getId(),
                    newsletter.getId(),
                    BASE_TIME.minusDays(i)
            ));
        }
        articleRepository.saveAll(articles);
    }

    private long countPreviousArticles(Long newsletterId, boolean isFixed) {
        return previousArticleRepository.findAll().stream()
                .filter(article -> article.getNewsletterId().equals(newsletterId))
                .filter(article -> article.isFixed() == isFixed)
                .count();
    }

    private String uniqueEmail(String prefix) {
        return uniqueValue(prefix) + "@bombom.news";
    }

    private String uniqueValue(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
