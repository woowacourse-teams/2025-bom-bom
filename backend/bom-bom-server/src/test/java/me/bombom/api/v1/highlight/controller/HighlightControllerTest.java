package me.bombom.api.v1.highlight.controller;

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
import me.bombom.api.v1.auth.handler.OAuth2LoginSuccessHandler;
import me.bombom.api.v1.highlight.domain.Highlight;
import me.bombom.api.v1.highlight.repository.HighlightRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
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
    private ArticleRepository articleRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private HighlightRepository highlightRepository;

    @MockitoBean
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    private List<Highlight> highlights;
    private CustomOAuth2User customOAuth2User;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAllInBatch();
        articleRepository.deleteAllInBatch();

        Member member = TestFixture.normalMemberFixture();
        memberRepository.save(member);

        List<Category> categories = TestFixture.createCategories();
        categoryRepository.saveAll(categories);

        List<Newsletter> newsletters = TestFixture.createNewsletters(categories);
        newsletterRepository.saveAll(newsletters);

        List<Article> articles = TestFixture.createArticles(member, newsletters);
        articleRepository.saveAll(articles);

        highlights = TestFixture.createHighlightFixtures(articles);
        highlightRepository.saveAll(highlights);

        // Argument Resolver를 위해 CustomOAuth2User 생성
        Map<String, Object> attributes = Map.of(
                "id", member.getId().toString(),
                "email", member.getEmail(),
                "name", member.getNickname()
        );
        customOAuth2User = new CustomOAuth2User(attributes, member);
    }

    private void setAuthentication() {
        // OAuth2AuthenticationToken 생성
        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                customOAuth2User,
                customOAuth2User.getAuthorities(),
                "registrationId"
        );

        // SecurityContext에 인증 정보 설정
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authToken);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void 하이라이트_생성_성공() throws Exception {
        // given
        setAuthentication();

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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated());
    }

    @Test
    void 하이라이트_수정_성공() throws Exception {
        // given
        setAuthentication();

        // when & then
        mockMvc.perform(patch("/api/v1/highlights/{id}", highlights.getFirst().getId())
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
        // given
        setAuthentication();

        // when & then
        mockMvc.perform(patch("/api/v1/highlights/{id}", highlights.getFirst().getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "color": "4caf50",
                                "memo": "수정된 메모"
                            }
                        """))
                .andExpect(status().isBadRequest());
    }
}
