package me.bombom.api.v1.bookmark.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.auth.handler.OAuth2LoginSuccessHandler;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
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

    @MockitoBean
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    private Member member;
    private CustomOAuth2User customOAuth2User;
    private OAuth2AuthenticationToken authToken;

    @BeforeEach
    void setUp() {
        bookmarkRepository.deleteAllInBatch();
        articleRepository.deleteAllInBatch();
        newsletterDetailRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        newsletterRepository.deleteAllInBatch();

        newsletterDetailRepository.saveAll(TestFixture.createNewsletterDetails());

        member = TestFixture.normalMemberFixture();
        memberRepository.save(member);

        List<Category> categories = TestFixture.createCategories();
        categoryRepository.saveAll(categories);

        List<NewsletterDetail> newsletterDetails = TestFixture.createNewsletterDetails();
        newsletterDetailRepository.saveAll(newsletterDetails);

        List<Newsletter> newsletters = TestFixture.createNewslettersWithDetails(categories, newsletterDetails);
        newsletterRepository.saveAll(newsletters);

        List<Article> articles = TestFixture.createArticles(member, newsletters);
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
    @DisplayName("북마크 목록 조회 성공")
    void getBookmarks_success() throws Exception {
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
    @DisplayName("북마크 상태 조회 성공")
    void getBookmarkStatus_success() throws Exception {
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
    @DisplayName("북마크 추가 성공")
    void addBookmark_success() throws Exception {
        // given
        Article article = articleRepository.findAll().get(1);

        // when & then
        mockMvc.perform(post("/api/v1/bookmarks/articles/" + article.getId())
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("북마크 삭제 성공")
    void deleteBookmark_success() throws Exception {
        // given
        Article article = articleRepository.findAll().get(0);

        // when & then
        mockMvc.perform(delete("/api/v1/bookmarks/articles/" + article.getId())
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("뉴스레터별 북마크 통계 조회 성공")
    void getBookmarkNewsletterStatistics_success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/bookmarks/statistics/newsletters")
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(1));
    }
}
