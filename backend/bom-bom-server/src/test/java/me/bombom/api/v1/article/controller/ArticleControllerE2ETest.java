package me.bombom.api.v1.article.controller;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.article.service.ArticleService;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.auth.handler.OAuth2LoginSuccessHandler;
import me.bombom.api.v1.auth.service.CustomOAuth2UserService;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterDetailRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.api.v1.reading.service.ReadingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("아티클 목록 조회 Controller E2E 테스트")
class ArticleControllerE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Autowired
    private ArticleService articleService;

    @MockitoBean
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @MockitoBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockitoBean
    private ReadingService readingService;

    private Member member;
    private List<Article> articles;
    private List<Newsletter> newsletters;
    private List<Category> categories;
    private CustomOAuth2User customOAuth2User;

    @BeforeEach
    void setUp() {
        // TestFixture를 적극 활용한 테스트 데이터 준비
        newsletterDetailRepository.saveAll(TestFixture.createNewsletterDetails());
        
        member = TestFixture.normalMemberFixture();
        memberRepository.save(member);

        categories = TestFixture.createCategories();
        categoryRepository.saveAll(categories);

        newsletters = TestFixture.createNewsletters(categories);
        newsletterRepository.saveAll(newsletters);

        articles = TestFixture.createArticles(member, newsletters);
        articleRepository.saveAll(articles);

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
    @DisplayName("기본 아티클 목록 조회 성공")
    void 아티클_목록_조회_성공() throws Exception {
        // given
        setAuthentication();

        // when & then
        mockMvc.perform(get("/api/v1/articles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.numberOfElements").value(4))
                .andExpect(jsonPath("$.empty").value(false));
    }

    @Test
    @DisplayName("경제 카테고리 아티클 목록 조회")
    void 경제_카테고리_아티클_목록_조회() throws Exception {
        // given
        setAuthentication();
        String economyCategory = categories.get(0).getName(); // "경제"

        // when & then
        mockMvc.perform(get("/api/v1/articles")
                        .param("category", economyCategory))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].newsletter.category").value("경제"));
    }

    @Test
    @DisplayName("테크 카테고리 아티클 목록 조회")
    void 테크_카테고리_아티클_목록_조회() throws Exception {
        // given
        setAuthentication();
        String techCategory = categories.get(1).getName(); // "테크"

        // when & then
        mockMvc.perform(get("/api/v1/articles")
                        .param("category", techCategory))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].newsletter.category").value("테크"));
    }

    @Test
    @DisplayName("푸드 카테고리 아티클 목록 조회")
    void 푸드_카테고리_아티클_목록_조회() throws Exception {
        // given
        setAuthentication();
        String foodCategory = categories.get(2).getName(); // "푸드"

        // when & then
        mockMvc.perform(get("/api/v1/articles")
                        .param("category", foodCategory))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].newsletter.category").value("푸드"))
                .andExpect(jsonPath("$.content[1].newsletter.category").value("푸드"));
    }

    @Test
    @DisplayName("뉴스 키워드 검색 아티클 목록 조회")
    void 뉴스_키워드_검색_아티클_목록_조회() throws Exception {
        // given
        setAuthentication();

        // when & then
        mockMvc.perform(get("/api/v1/articles")
                        .param("keyword", "뉴스"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].title").value("뉴스"))
                .andExpect(jsonPath("$.content[1].title").value("뉴스"));
    }

    @Test
    @DisplayName("레터 키워드 검색 아티클 목록 조회")
    void 레터_키워드_검색_아티클_목록_조회() throws Exception {
        // given
        setAuthentication();

        // when & then
        mockMvc.perform(get("/api/v1/articles")
                        .param("keyword", "레터"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].title").value("레터"))
                .andExpect(jsonPath("$.content[1].title").value("레터"));
    }

    @Test
    @DisplayName("존재하지않는 키워드 검색 아티클 목록 조회")
    void 존재하지않는_키워드_검색_아티클_목록_조회() throws Exception {
        // given
        setAuthentication();

        // when & then
        mockMvc.perform(get("/api/v1/articles")
                        .param("keyword", "존재하지않는키워드"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @DisplayName("기본값이 DESC 정렬인지 확인")
    void 기본값이_DESC_정렬인지_확인() throws Exception {
        // given
        setAuthentication();

        // when & then - 정렬 파라미터 없는 기본값
        MvcResult defaultResult = mockMvc.perform(get("/api/v1/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(4))
                .andReturn();

        // when & then - 명시적 DESC 정렬
        MvcResult descResult = mockMvc.perform(get("/api/v1/articles")
                        .param("sorted", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(4))
                .andReturn();

        // 기본값이 DESC와 같은지 확인
        String defaultContent = defaultResult.getResponse().getContentAsString();
        String descContent = descResult.getResponse().getContentAsString();

        JsonNode defaultJson = objectMapper.readTree(defaultContent);
        String defaultFirstDateTime = defaultJson.get("content").get(0).get("arrivedDateTime").asText();

        assertSoftly(softly -> {
            softly.assertThat(defaultContent).isEqualTo(descContent);
            softly.assertThat(defaultFirstDateTime).isEqualTo("2025-07-15T09:55:00");
        });
    }

    @Test
    @DisplayName("DESC 정렬 아티클 목록 조회")
    void DESC_정렬_아티클_목록_조회() throws Exception {
        // given
        setAuthentication();

        // when & then
        MvcResult result = mockMvc.perform(get("/api/v1/articles")
                        .param("sorted", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(4))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        JsonNode jsonResponse = objectMapper.readTree(responseContent);
        String firstDateTime = jsonResponse.get("content").get(0).get("arrivedDateTime").asText();
        String lastDateTime = jsonResponse.get("content").get(3).get("arrivedDateTime").asText();

        assertSoftly(softly -> {
            softly.assertThat(firstDateTime).isEqualTo("2025-07-15T09:55:00"); // 최신
            softly.assertThat(lastDateTime).isEqualTo("2025-07-14T10:00:00");  // 오래됨
        });
    }

    @Test
    @DisplayName("ASC 정렬 아티클 목록 조회")
    void ASC_정렬_아티클_목록_조회() throws Exception {
        // given
        setAuthentication();

        // when & then
        MvcResult result = mockMvc.perform(get("/api/v1/articles")
                        .param("sorted", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(4))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        JsonNode jsonResponse = objectMapper.readTree(responseContent);
        String firstDateTime = jsonResponse.get("content").get(0).get("arrivedDateTime").asText();
        String lastDateTime = jsonResponse.get("content").get(3).get("arrivedDateTime").asText();

        assertSoftly(softly -> {
            softly.assertThat(firstDateTime).isEqualTo("2025-07-14T10:00:00");  // 오래됨
            softly.assertThat(lastDateTime).isEqualTo("2025-07-15T09:55:00");   // 최신
        });
    }

    @Test
    @DisplayName("첫번째 페이지 아티클 목록 조회")
    void 첫번째_페이지_아티클_목록_조회() throws Exception {
        // given
        setAuthentication();

        // when & then
        mockMvc.perform(get("/api/v1/articles")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isNotEmpty())
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    @DisplayName("두번째 페이지 아티클 목록 조회")
    void 두번째_페이지_아티클_목록_조회() throws Exception {
        // given
        setAuthentication();

        // when & then
        mockMvc.perform(get("/api/v1/articles")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    @DisplayName("날짜 필터링 아티클 목록 조회")
    void 날짜_필터링_아티클_목록_조회() throws Exception {
        // given
        setAuthentication();
        LocalDate baseDate = LocalDate.of(2025, 7, 15);

        // when & then - 특정 날짜로 필터링
        mockMvc.perform(get("/api/v1/articles")
                        .param("date", baseDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(3)); // 하루 전 제외하고 3개
    }

    @Test
    @DisplayName("카테고리 키워드 날짜 복합 필터링 아티클 목록 조회")
    void 카테고리_키워드_날짜_복합_필터링_아티클_목록_조회() throws Exception {
        // given
        setAuthentication();
        String foodCategory = categories.get(2).getName(); // "푸드"
        LocalDate baseDate = LocalDate.of(2025, 7, 15);

        // when & then - 카테고리 + 키워드 + 날짜 복합 필터링
        mockMvc.perform(get("/api/v1/articles")
                        .param("category", foodCategory)
                        .param("keyword", "레터")
                        .param("date", baseDate.toString())
                        .param("sorted", "desc")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1)) // 조건에 맞는 1개
                .andExpect(jsonPath("$.content[0].title").value("레터"))
                .andExpect(jsonPath("$.content[0].newsletter.category").value("푸드"));
    }

    @Test
    @DisplayName("인증되지 않은 사용자의 아티클 목록 조회 시 401 에러")
    void 인증되지않은_사용자_아티클_목록_조회시_예외() throws Exception {
        // when & then - 인증 정보 없이 요청 (setAuthentication() 호출 안함)
        mockMvc.perform(get("/api/v1/articles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists());
    }
}
