package me.bombom.api.v1.bookmark.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.*;

import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.bookmark.domain.Bookmark;
import me.bombom.api.v1.bookmark.dto.BookmarkResponse;
import me.bombom.api.v1.bookmark.repository.BookmarkRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import me.bombom.api.v1.TestJpaAuditingConfig;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

@DataJpaTest
@Import({BookmarkService.class, QuerydslConfig.class, TestJpaAuditingConfig.class})
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

    @BeforeEach
    void setUp() {
        member = TestFixture.normalMemberFixture();
        memberRepository.save(member);
        categories = TestFixture.createCategories();
        categoryRepository.saveAll(categories);
        newsletters = TestFixture.createNewsletters(categories);
        newsletterRepository.saveAll(newsletters);
        article = TestFixture.createArticle("테스트 아티클", member.getId(), newsletters.get(0).getId(), java.time.LocalDateTime.now());
        articleRepository.save(article);
    }

    @Test
    void 북마크_저장_및_조회_테스트() {
        // given
        bookmarkService.save(member.getId(), article.getId());

        // when
        Page<BookmarkResponse> bookmarks = bookmarkService.getBookmarks(member.getId(), PageRequest.of(0, 10));

        // then
        assertSoftly(softly -> {
            softly.assertThat(bookmarks.getContent()).hasSize(1);
            softly.assertThat(bookmarks.getContent().getFirst().articleResponse().articleId()).isEqualTo(article.getId());
        });
    }

    @Test
    void 북마크_상태_조회_테스트() {
        // given
        bookmarkService.save(member.getId(), article.getId());

        // when
        boolean exists = bookmarkService.getBookmarkStatus(member.getId(), article.getId());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void 북마크_삭제_테스트() {
        // given
        bookmarkService.save(member.getId(), article.getId());
        assertThat(bookmarkRepository.count()).isEqualTo(1);

        // when
        bookmarkService.deleteByArticleId(member.getId(), article.getId());

        // then
        assertThat(bookmarkRepository.count()).isZero();
    }

    @Test
    void 북마크_중복_저장_예외_테스트() {
        // given
        bookmarkService.save(member.getId(), article.getId());

        // when & then
        assertThatThrownBy(() -> bookmarkService.save(member.getId(), article.getId()))
            .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 북마크_createdAt_DESC_정렬_테스트() {
        // given
        bookmarkService.save(member.getId(), article.getId());
        Article article2 = TestFixture.createArticle("두번째 아티클", member.getId(), newsletters.get(0).getId(), java.time.LocalDateTime.now().plusMinutes(1));
        articleRepository.save(article2);
        bookmarkService.save(member.getId(), article2.getId());

        List<Bookmark> bookmarks = bookmarkRepository.findAll();
        Bookmark first = bookmarks.stream().filter(b -> b.getArticleId().equals(article.getId())).findFirst().orElseThrow();
        Bookmark second = bookmarks.stream().filter(b -> b.getArticleId().equals(article2.getId())).findFirst().orElseThrow();

        // when: DESC 정렬
        Page<BookmarkResponse> descBookmarks = bookmarkService.getBookmarks(
            member.getId(),
            PageRequest.of(0, 10, Sort.by(Direction.DESC, "createdAt"))
        );

        // then
        if (first.getCreatedAt().isBefore(second.getCreatedAt())) {
            assertThat(descBookmarks.getContent().get(0).articleResponse().articleId()).isEqualTo(second.getArticleId());
            assertThat(descBookmarks.getContent().get(1).articleResponse().articleId()).isEqualTo(first.getArticleId());
        } else {
            assertThat(descBookmarks.getContent().get(0).articleResponse().articleId()).isEqualTo(first.getArticleId());
            assertThat(descBookmarks.getContent().get(1).articleResponse().articleId()).isEqualTo(second.getArticleId());
        }
    }

    @Test
    void 북마크_createdAt_ASC_정렬_테스트() {
        // given
        bookmarkService.save(member.getId(), article.getId());
        Article article2 = TestFixture.createArticle("두번째 아티클", member.getId(), newsletters.get(0).getId(), java.time.LocalDateTime.now().plusMinutes(1));
        articleRepository.save(article2);
        bookmarkService.save(member.getId(), article2.getId());

        List<Bookmark> bookmarks = bookmarkRepository.findAll();
        Bookmark first = bookmarks.stream().filter(b -> b.getArticleId().equals(article.getId())).findFirst().orElseThrow();
        Bookmark second = bookmarks.stream().filter(b -> b.getArticleId().equals(article2.getId())).findFirst().orElseThrow();

        // when: ASC 정렬
        Page<BookmarkResponse> ascBookmarks = bookmarkService.getBookmarks(
            member.getId(),
            PageRequest.of(0, 10, Sort.by(Direction.ASC, "createdAt"))
        );

        // then
        if (first.getCreatedAt().isBefore(second.getCreatedAt())) {
            assertThat(ascBookmarks.getContent().get(0).articleResponse().articleId()).isEqualTo(first.getArticleId());
            assertThat(ascBookmarks.getContent().get(1).articleResponse().articleId()).isEqualTo(second.getArticleId());
        } else {
            assertThat(ascBookmarks.getContent().get(0).articleResponse().articleId()).isEqualTo(second.getArticleId());
            assertThat(ascBookmarks.getContent().get(1).articleResponse().articleId()).isEqualTo(first.getArticleId());
        }
    }
} 
