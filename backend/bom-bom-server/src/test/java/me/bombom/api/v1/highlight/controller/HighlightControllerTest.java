package me.bombom.api.v1.highlight.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import me.bombom.api.v1.highlight.domain.Highlight;
import me.bombom.api.v1.highlight.repository.HighlightRepository;
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
class HighlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private NewsletterRepository newsletterRepository;

    @Autowired
    private NewsletterDetailRepository newsletterDetailRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private HighlightRepository highlightRepository;

    private List<Highlight> highlights;
    private CustomOAuth2User customOAuth2User;
    private OAuth2AuthenticationToken authToken;
    private Member member;
    private List<Newsletter> newsletters;
    private List<Article> articles;

    @BeforeEach
    void setUp() {
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

        highlights = TestFixture.createHighlightFixtures(articles);
        highlightRepository.saveAll(highlights);

        // Argument Resolver를 위해 CustomOAuth2User 생성
        Map<String, Object> attributes = Map.of(
                "id", member.getId().toString(),
                "email", member.getEmail(),
                "name", member.getNickname()
        );
        customOAuth2User = new CustomOAuth2User(attributes, member, null, null);
        
        // OAuth2AuthenticationToken 생성
        authToken = new OAuth2AuthenticationToken(
                customOAuth2User,
                customOAuth2User.getAuthorities(),
                "registrationId"
        );
    }

    @Test
    void 하이라이트_생성_성공() throws Exception {
        // when & then
        String content = String.format("""
        {
          "location": {
            "startOffset": 0,
            "startXPath": "div[0]/p[0]",
            "endOffset": 10,
            "endXPath": "div[0]/p[0]"
          },
          "articleId": %d,
          "color": "#ffeb3b",
          "text": "하이라이트할 텍스트",
          "memo": "메모 내용 (선택사항)"
        }
        """, highlights.getFirst().getArticleId());
        mockMvc.perform(post("/api/v1/highlights")
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated());
    }

    @Test
    void 하이라이트_수정_성공() throws Exception {
        // when & then
        mockMvc.perform(patch("/api/v1/highlights/{id}", highlights.getFirst().getId())
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "color": "#4caf50"
                            }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.color").value("#4caf50"));
    }

    @Test
    void 하이라이트_수정_포맷에_맞지_않는_color_입력() throws Exception {
        // when & then
        mockMvc.perform(patch("/api/v1/highlights/{id}", highlights.getFirst().getId())
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "color": "4caf50",
                                "memo": "수정된 메모"
                            }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 아티클로_하이라이트를_필터링해_최신순으로_조회한다() throws Exception {
        mockMvc.perform(get("/api/v1/highlights")
                        .with(authentication(authToken))
                        .queryParam("articleId", articles.getFirst().getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].text").value("두 번째 하이라이트"))
                .andExpect(jsonPath("$.content[1].text").value("첫 번째 하이라이트"));
    }

    @Test
    void 뉴스레터로_하이라이트를_필터링한다() throws Exception {
        mockMvc.perform(get("/api/v1/highlights")
                        .with(authentication(authToken))
                        .queryParam("newsletterId", newsletters.get(2).getId().toString())
                        .queryParam("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void 중복된_위치에_하이라이트를_생성해도_건수가_증가하지_않는다() throws Exception {
        long beforeCount = highlightRepository.count();
        String content = String.format("""
                {
                  "location": {
                    "startOffset": 0,
                    "startXPath": "div[0]/p[0]",
                    "endOffset": 10,
                    "endXPath": "div[0]/p[0]"
                  },
                  "articleId": %d,
                  "color": "#ffeb3b",
                  "text": "중복 위치",
                  "memo": "메모"
                }
                """, articles.getFirst().getId());

        mockMvc.perform(post("/api/v1/highlights")
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated());

        assertThat(highlightRepository.count()).isEqualTo(beforeCount);
    }

    @Test
    void 존재하지_않는_아티클에는_하이라이트를_생성할_수_없다() throws Exception {
        String content = """
                {
                  "location": {
                    "startOffset": 0,
                    "startXPath": "div[0]/p[0]",
                    "endOffset": 10,
                    "endXPath": "div[0]/p[0]"
                  },
                  "articleId": 99999,
                  "color": "#ffeb3b",
                  "text": "존재하지 않는 글",
                  "memo": "메모"
                }
                """;

        mockMvc.perform(post("/api/v1/highlights")
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isNotFound());
    }

    @Test
    void 하이라이트를_삭제한다() throws Exception {
        Long highlightId = highlights.getFirst().getId();

        mockMvc.perform(delete("/api/v1/highlights/{id}", highlightId)
                        .with(authentication(authToken)))
                .andExpect(status().isNoContent());

        assertThat(highlightRepository.findById(highlightId)).isEmpty();
    }

    @Test
    void 존재하지_않는_하이라이트를_수정하면_404를_반환한다() throws Exception {
        mockMvc.perform(patch("/api/v1/highlights/{id}", 99999)
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "color": "#9c27b0"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void 뉴스레터별_하이라이트_통계를_조회한다() throws Exception {
        mockMvc.perform(get("/api/v1/highlights/statistics/newsletters")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(highlights.size()));
    }
}
