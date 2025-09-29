package me.bombom.api.v1.withdraw.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.bookmark.domain.Bookmark;
import me.bombom.api.v1.bookmark.repository.BookmarkRepository;
import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.highlight.domain.Highlight;
import me.bombom.api.v1.highlight.repository.HighlightRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.enums.Gender;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.api.v1.reading.domain.ContinueReading;
import me.bombom.api.v1.reading.repository.ContinueReadingRepository;
import me.bombom.api.v1.withdraw.domain.WithdrawnMember;
import me.bombom.api.v1.withdraw.repository.WithdrawnMemberRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@IntegrationTest
class WithdrawServiceTest {

    @Autowired
    private WithdrawService withdrawService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private WithdrawnMemberRepository withdrawnMemberRepository;

    @Autowired
    private HighlightRepository highlightRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private NewsletterRepository newsletterRepository;

    @Autowired
    private ContinueReadingRepository continueReadingRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private ArticleRepository articleRepository;

    List<Category> categories;
    List<Newsletter> newsletters;
    List<Article> articles;
    Member member;
    List<Highlight> highlights;

    @BeforeEach
    public void setup() {
        member = memberRepository.save(TestFixture.normalMemberFixture());
        categories = TestFixture.createCategories();
        categoryRepository.saveAll(categories);
        newsletters = TestFixture.createNewsletters(categories);
        newsletterRepository.saveAll(newsletters);
        articles = TestFixture.createArticles(member, newsletters);
        articleRepository.saveAll(articles);
        highlights = TestFixture.createHighlightFixtures(articles);
        highlightRepository.saveAll(highlights);
    }

    @Test
    void 회원_탈퇴_시_탈퇴_회원_정보로_이전된다() {
        // given
        continueReadingRepository.save(ContinueReading.builder()
                .memberId(member.getId())
                .dayCount(10)
                .build());
        bookmarkRepository.save(Bookmark.builder()
                .memberId(member.getId())
                .articleId(articles.getFirst().getId())
                .build());

        // when
        withdrawService.migrateDeletedMember(member);

        // then
        List<WithdrawnMember> withdrawn = withdrawnMemberRepository.findAll();
        assertSoftly(softly -> {
            softly.assertThat(withdrawn).hasSize(1);
            softly.assertThat(withdrawn.getFirst().getEmail()).isEqualTo("email");
            softly.assertThat(withdrawn.getFirst().getContinueReading()).isEqualTo(10);
            softly.assertThat(withdrawn.getFirst().getBookmarkedCount()).isEqualTo(1);
            softly.assertThat(withdrawn.getFirst().getHighlightCount()).isEqualTo(6);
        });
    }

    @Test
    void 만료일이_오늘인_탈퇴_회원_정보는_삭제된다() {
        // given
        WithdrawnMember expired = WithdrawnMember.builder()
                .memberId(1L)
                .email("expired@test.com")
                .gender(Gender.MALE)
                .joinedDate(LocalDate.now().minusDays(200))
                .deletedDate(LocalDate.now().minusDays(90))
                .expireDate(LocalDate.now()) // 오늘 만료
                .build();
        withdrawnMemberRepository.save(expired);

        WithdrawnMember notExpired = WithdrawnMember.builder()
                .memberId(2L)
                .email("valid@test.com")
                .gender(Gender.FEMALE)
                .joinedDate(LocalDate.now().minusDays(200))
                .deletedDate(LocalDate.now().minusDays(79))
                .expireDate(LocalDate.now().plusDays(1)) // 아직 유효
                .build();
        withdrawnMemberRepository.save(notExpired);

        // when
        withdrawService.deleteExpiredWithdrawnMembers();

        // then
        List<WithdrawnMember> remaining = withdrawnMemberRepository.findAll();
        assertSoftly(softly -> {
            softly.assertThat(remaining).hasSize(1);
            softly.assertThat(remaining.get(0).getEmail()).isEqualTo("valid@test.com");
        });
    }
}
