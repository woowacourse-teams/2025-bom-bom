package me.bombom.api.v1.bookmark.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.bookmark.domain.Bookmark;
import me.bombom.api.v1.bookmark.dto.response.BookmarkResponse;
import me.bombom.api.v1.bookmark.dto.response.BookmarkStatusResponse;
import me.bombom.api.v1.bookmark.dto.response.GetBookmarkNewsletterStatisticsResponse;
import me.bombom.api.v1.bookmark.repository.BookmarkRepository;
import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.enums.Gender;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

@DataJpaTest
@Import({BookmarkService.class, QuerydslConfig.class})
class BookmarkServiceTest {

    @Autowired
    private BookmarkService bookmarkService;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private NewsletterRepository newsletterRepository;

    private Member member;
    private Article article;
    private List<Category> categories;
    private List<Newsletter> newsletters;
    private List<Article> articles;

    @BeforeEach
    void setUp() {
        member = TestFixture.normalMemberFixture();
        memberRepository.save(member);
        categories = TestFixture.createCategories();
        categoryRepository.saveAll(categories);
        newsletters = TestFixture.createNewsletters(categories);
        newsletterRepository.saveAll(newsletters);
        articles = TestFixture.createArticles(member, newsletters);
        articleRepository.saveAll(articles);
        article = TestFixture.createArticle("테스트 아티클", member.getId(), newsletters.get(0).getId(),
                java.time.LocalDateTime.now());
        articleRepository.save(article);
    }

    @Test
    void 북마크_저장_및_조회_테스트() {
        // given
        bookmarkService.addBookmark(member.getId(), article.getId());

        // when
        Page<BookmarkResponse> bookmarks = bookmarkService.getBookmarks(member.getId(), PageRequest.of(0, 10));

        // then
        assertSoftly(softly -> {
            softly.assertThat(bookmarks.getContent()).hasSize(1);
            softly.assertThat(bookmarks.getContent().getFirst().articleId()).isEqualTo(article.getId());
        });
    }

    @Test
    void 북마크_상태_조회_테스트() {
        // given
        bookmarkService.addBookmark(member.getId(), article.getId());

        // when
        BookmarkStatusResponse status = bookmarkService.getBookmarkStatus(member.getId(), article.getId());

        // then
        assertThat(status.bookmarkStatus()).isTrue();
    }

    @Test
    void 북마크_삭제_테스트() {
        // given
        bookmarkService.addBookmark(member.getId(), article.getId());
        assertThat(bookmarkRepository.count()).isEqualTo(1);

        // when
        bookmarkService.deleteBookmark(member.getId(), article.getId());

        // then
        assertThat(bookmarkRepository.count()).isZero();
    }

    @Test
    void 북마크_중복_저장_스킵_테스트() {
        // given
        bookmarkService.addBookmark(member.getId(), article.getId());
        long countAfterFirst = bookmarkRepository.count();

        // when
        bookmarkService.addBookmark(member.getId(), article.getId());
        long countAfterSecond = bookmarkRepository.count();

        // then
        assertThat(countAfterFirst).isEqualTo(1);
        assertThat(countAfterSecond).isEqualTo(1);
    }

    @Test
    void 북마크_createdAt_DESC_정렬_테스트() {
        // given
        bookmarkService.addBookmark(member.getId(), article.getId());
        Article article2 = TestFixture.createArticle("두번째 아티클", member.getId(), newsletters.get(0).getId(),
                java.time.LocalDateTime.now().plusMinutes(1));
        articleRepository.save(article2);
        bookmarkService.addBookmark(member.getId(), article2.getId());

        List<Bookmark> bookmarks = bookmarkRepository.findAll();
        Bookmark first = bookmarks.stream().filter(b -> b.getArticleId().equals(article.getId())).findFirst()
                .orElseThrow();
        Bookmark second = bookmarks.stream().filter(b -> b.getArticleId().equals(article2.getId())).findFirst()
                .orElseThrow();

        // when: DESC 정렬
        Page<BookmarkResponse> descBookmarks = bookmarkService.getBookmarks(
                member.getId(),
                PageRequest.of(0, 10, Sort.by(Direction.DESC, "createdAt"))
        );

        // then
        if (first.getCreatedAt().isBefore(second.getCreatedAt())) {
            assertThat(descBookmarks.getContent().get(0).articleId()).isEqualTo(second.getArticleId());
            assertThat(descBookmarks.getContent().get(1).articleId()).isEqualTo(first.getArticleId());
        } else {
            assertThat(descBookmarks.getContent().get(0).articleId()).isEqualTo(first.getArticleId());
            assertThat(descBookmarks.getContent().get(1).articleId()).isEqualTo(second.getArticleId());
        }
    }

    @Test
    void 북마크_createdAt_ASC_정렬_테스트() {
        // given
        bookmarkService.addBookmark(member.getId(), article.getId());
        Article article2 = TestFixture.createArticle("두번째 아티클", member.getId(), newsletters.get(0).getId(),
                java.time.LocalDateTime.now().plusMinutes(1));
        articleRepository.save(article2);
        bookmarkService.addBookmark(member.getId(), article2.getId());

        List<Bookmark> bookmarks = bookmarkRepository.findAll();
        Bookmark first = bookmarks.stream().filter(b -> b.getArticleId().equals(article.getId())).findFirst()
                .orElseThrow();
        Bookmark second = bookmarks.stream().filter(b -> b.getArticleId().equals(article2.getId())).findFirst()
                .orElseThrow();

        // when: ASC 정렬
        Page<BookmarkResponse> ascBookmarks = bookmarkService.getBookmarks(
                member.getId(),
                PageRequest.of(0, 10, Sort.by(Direction.ASC, "createdAt"))
        );

        // then
        if (first.getCreatedAt().isBefore(second.getCreatedAt())) {
            assertThat(ascBookmarks.getContent().get(0).articleId()).isEqualTo(first.getArticleId());
            assertThat(ascBookmarks.getContent().get(1).articleId()).isEqualTo(second.getArticleId());
        } else {
            assertThat(ascBookmarks.getContent().get(0).articleId()).isEqualTo(second.getArticleId());
            assertThat(ascBookmarks.getContent().get(1).articleId()).isEqualTo(first.getArticleId());
        }
    }

    @Test
    void 다른_사용자_아티클_북마크_예외_테스트() {
        // given
        Member otherMember = Member.builder()
                .provider("provider2")
                .providerId("providerId2")
                .email("other@email.com")
                .nickname("other")
                .gender(Gender.FEMALE)
                .roleId(1L)
                .build();
        otherMember = memberRepository.save(otherMember);
        Article otherArticle = TestFixture.createArticle("타인 아티클", otherMember.getId(), newsletters.get(0).getId(),
                java.time.LocalDateTime.now().plusMinutes(2));
        articleRepository.save(otherArticle);

        // when & then
        assertThatThrownBy(() -> bookmarkService.addBookmark(member.getId(), otherArticle.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.FORBIDDEN_RESOURCE);
    }

    @Test
    void 다른_사용자_아티클_북마크_삭제_예외_테스트() {
        // given
        Member otherMember = Member.builder()
                .provider("provider2")
                .providerId("providerId2")
                .email("other@email.com")
                .nickname("other")
                .gender(Gender.FEMALE)
                .roleId(1L)
                .build();
        otherMember = memberRepository.save(otherMember);
        Article otherArticle = TestFixture.createArticle("타인 아티클", otherMember.getId(), newsletters.get(0).getId(),
                java.time.LocalDateTime.now().plusMinutes(2));
        articleRepository.save(otherArticle);
        bookmarkService.addBookmark(otherMember.getId(), otherArticle.getId());

        // when & then
        assertThatThrownBy(() -> bookmarkService.deleteBookmark(member.getId(), otherArticle.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.FORBIDDEN_RESOURCE);
    }

    @Test
    void 전체_뉴스레터_별_북마크_개수를_조회한다() {
        // given
        bookmarkService.addBookmark(member.getId(), articles.get(0).getId());
        bookmarkService.addBookmark(member.getId(), articles.get(1).getId());
        bookmarkService.addBookmark(member.getId(), articles.get(2).getId());
        bookmarkService.addBookmark(member.getId(), articles.get(3).getId());

        // when
        GetBookmarkNewsletterStatisticsResponse result = bookmarkService.getBookmarkNewsletterStatistics(member);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.totalCount()).isEqualTo(4);
            softly.assertThat(result.newsletters().get(0).newsletter()).isEqualTo("뉴스픽");
            softly.assertThat(result.newsletters().get(0).count()).isEqualTo(1);
            softly.assertThat(result.newsletters().get(1).newsletter()).isEqualTo("IT타임즈");
            softly.assertThat(result.newsletters().get(1).count()).isEqualTo(1);
            softly.assertThat(result.newsletters().get(2).newsletter()).isEqualTo("비즈레터");
            softly.assertThat(result.newsletters().get(2).count()).isEqualTo(2);
        });
    }
} 
