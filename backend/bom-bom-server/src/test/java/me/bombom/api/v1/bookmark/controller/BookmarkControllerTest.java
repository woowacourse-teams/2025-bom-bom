package me.bombom.api.v1.bookmark.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.bookmark.domain.Bookmark;
import me.bombom.api.v1.bookmark.repository.BookmarkRepository;
import me.bombom.api.v1.member.domain.Member;
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
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
class BookmarkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private NewsletterRepository newsletterRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private NewsletterDetailRepository newsletterDetailRepository;

    private Member member;
    private CustomOAuth2User customOAuth2User;
    private OAuth2AuthenticationToken authToken;
    private List<Newsletter> newsletters;
    private List<Article> articles;

    @BeforeEach
    void setUp() {
        newsletterDetailRepository.saveAll(TestFixture.createNewsletterDetails());

        member = TestFixture.normalMemberFixture();
        memberRepository.save(member);

        List<Category> categories = TestFixture.createCategories();
        categoryRepository.saveAll(categories);

        List<NewsletterDetail> newsletterDetails = TestFixture.createNewsletterDetails();
        newsletterDetailRepository.saveAll(newsletterDetails);

        newsletters = TestFixture.createNewslettersWithDetails(categories, newsletterDetails);
        newsletterRepository.saveAll(newsletters);

        articles = TestFixture.createArticles(member, newsletters);
        articleRepository.saveAll(articles);

        Bookmark bookmark = Bookmark.builder()
                .memberId(member.getId())
                .articleId(articles.get(0).getId())
                .build();
        bookmarkRepository.save(bookmark);

        Map<String, Object> attributes = Map.of(
                "id", member.getId().toString(),
                "email", member.getEmail(),
                "name", member.getNickname()
        );
        customOAuth2User = new CustomOAuth2User(attributes, member, null, null);
        
        authToken = new OAuth2AuthenticationToken(
                customOAuth2User,
                customOAuth2User.getAuthorities(),
                "registrationId"
        );
    }

    @Test
    void 북마크_목록을_조회한다() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/bookmarks")
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("뉴스"));
    }

    @Test
    void 북마크_상태를_조회한다() throws Exception {
        // given
        Article article = articleRepository.findAll().get(0);

        // when & then
        mockMvc.perform(get("/api/v1/bookmarks/status/articles/" + article.getId())
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookmarkStatus").value(true));
    }

    @Test
    void 북마크를_추가한다() throws Exception {
        // given
        Article article = articleRepository.findAll().get(1);

        // when & then
        mockMvc.perform(post("/api/v1/bookmarks/articles/" + article.getId())
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void 북마크를_삭제한다() throws Exception {
        // given
        Article article = articleRepository.findAll().get(0);

        // when & then
        mockMvc.perform(delete("/api/v1/bookmarks/articles/" + article.getId())
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void 뉴스레터별_북마크_통계를_조회한다() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/bookmarks/statistics/newsletters")
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(1));
    }

    @Test
    void 뉴스레터로_북마크_목록을_필터링한다() throws Exception {
        Article article = articles.stream()
                .filter(candidate -> candidate.getNewsletterId().equals(newsletters.get(1).getId()))
                .findFirst()
                .orElseThrow();
        bookmarkRepository.save(Bookmark.builder()
                .memberId(member.getId())
                .articleId(article.getId())
                .build());

        mockMvc.perform(get("/api/v1/bookmarks")
                        .with(authentication(authToken))
                        .queryParam("newsletterId", newsletters.get(1).getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].articleId").value(article.getId()));
    }

    @Test
    void 같은_아티클을_중복_북마크해도_한_건만_저장한다() throws Exception {
        Article article = articles.get(1);

        mockMvc.perform(post("/api/v1/bookmarks/articles/{articleId}", article.getId())
                        .with(authentication(authToken)))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/v1/bookmarks/articles/{articleId}", article.getId())
                        .with(authentication(authToken)))
                .andExpect(status().isNoContent());

        assertThat(bookmarkRepository.countAllByMemberIdAndNewsletterId(
                member.getId(),
                article.getNewsletterId()
        )).isEqualTo(1);
    }

    @Test
    void 다른_사용자의_아티클은_북마크할_수_없다() throws Exception {
        Member other = memberRepository.save(TestFixture.createUniqueMember("other", "bookmark-other"));
        Article otherArticle = articleRepository.save(TestFixture.createArticle(
                "다른 사용자의 글",
                other.getId(),
                newsletters.getFirst().getId(),
                java.time.LocalDateTime.of(2026, 1, 1, 0, 0)
        ));

        mockMvc.perform(post("/api/v1/bookmarks/articles/{articleId}", otherArticle.getId())
                        .with(authentication(authToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void 다른_사용자의_아티클_북마크는_삭제할_수_없다() throws Exception {
        Member other = memberRepository.save(TestFixture.createUniqueMember("other", "bookmark-delete-other"));
        Article otherArticle = articleRepository.save(TestFixture.createArticle(
                "다른 사용자의 글",
                other.getId(),
                newsletters.getFirst().getId(),
                java.time.LocalDateTime.of(2026, 1, 1, 0, 0)
        ));

        mockMvc.perform(delete("/api/v1/bookmarks/articles/{articleId}", otherArticle.getId())
                        .with(authentication(authToken)))
                .andExpect(status().isForbidden());
    }
}
