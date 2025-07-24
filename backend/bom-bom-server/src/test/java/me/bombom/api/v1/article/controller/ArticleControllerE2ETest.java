package me.bombom.api.v1.article.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterDetailRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"frontend.base-url=http://localhost:3000"})
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

    private Member member;
    private List<Article> articles;
    private List<Newsletter> newsletters;
    private List<Category> categories;

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
    }

    @Test
    void 아티클_목록_조회_성공() throws Exception {
        // when & then
        MvcResult result = mockMvc.perform(get("/api/v1/articles")
                        .param("memberId", member.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(4))
                .andReturn();

        // 응답 본문 검증
        String responseContent = result.getResponse().getContentAsString();
        JsonNode jsonResponse = objectMapper.readTree(responseContent);
        
        assertSoftly(softly -> {
            softly.assertThat(jsonResponse.get("totalElements").asInt()).isEqualTo(4);
            softly.assertThat(jsonResponse.get("content").size()).isEqualTo(4);
            softly.assertThat(jsonResponse.get("first").asBoolean()).isTrue();
            softly.assertThat(jsonResponse.get("last").asBoolean()).isTrue();
            softly.assertThat(jsonResponse.get("numberOfElements").asInt()).isEqualTo(4);
            softly.assertThat(jsonResponse.get("empty").asBoolean()).isFalse();
        });
    }

    @Test
    void 경제_카테고리_아티클_목록_조회() throws Exception {
        // given
        String economyCategory = categories.get(0).getName(); // "경제"

        // when & then
        mockMvc.perform(get("/api/v1/articles")
                        .param("memberId", member.getId().toString())
                        .param("category", economyCategory))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].newsletter.category").value("경제"));
    }

    @Test
    void 테크_카테고리_아티클_목록_조회() throws Exception {
        // given
        String techCategory = categories.get(1).getName(); // "테크"

        // when & then
        mockMvc.perform(get("/api/v1/articles")
                        .param("memberId", member.getId().toString())
                        .param("category", techCategory))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].newsletter.category").value("테크"));
    }

    @Test
    void 푸드_카테고리_아티클_목록_조회() throws Exception {
        // given
        String foodCategory = categories.get(2).getName(); // "푸드"

        // when & then
        mockMvc.perform(get("/api/v1/articles")
                        .param("memberId", member.getId().toString())
                        .param("category", foodCategory))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].newsletter.category").value("푸드"))
                .andExpect(jsonPath("$.content[1].newsletter.category").value("푸드"));
    }

    @Test
    void 뉴스_키워드_검색_아티클_목록_조회() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/articles")
                        .param("memberId", member.getId().toString())
                        .param("keyword", "뉴스"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].title").value("뉴스"))
                .andExpect(jsonPath("$.content[1].title").value("뉴스"));
    }

    @Test
    void 레터_키워드_검색_아티클_목록_조회() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/articles")
                        .param("memberId", member.getId().toString())
                        .param("keyword", "레터"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].title").value("레터"))
                .andExpect(jsonPath("$.content[1].title").value("레터"));
    }

    @Test
    void 존재하지않는_키워드_검색_아티클_목록_조회() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/articles")
                        .param("memberId", member.getId().toString())
                        .param("keyword", "존재하지않는키워드"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void 기본값이_DESC_정렬인지_확인() throws Exception {
        // when & then - 정렬 파라미터 없는 기본값
        MvcResult defaultResult = mockMvc.perform(get("/api/v1/articles")
                        .param("memberId", member.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(4))
                .andReturn();

        // when & then - 명시적 DESC 정렬
        MvcResult descResult = mockMvc.perform(get("/api/v1/articles")
                        .param("memberId", member.getId().toString())
                        .param("sorted", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(4))
                .andReturn();

        // 기본값이 DESC와 같은지 확인
        String defaultContent = defaultResult.getResponse().getContentAsString();
        String descContent = descResult.getResponse().getContentAsString();

        assertThat(defaultContent).isEqualTo(descContent);
    }

    @Test
    void DESC_정렬_아티클_목록_조회() throws Exception {
        // when & then
        MvcResult result = mockMvc.perform(get("/api/v1/articles")
                        .param("memberId", member.getId().toString())
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
    void ASC_정렬_아티클_목록_조회() throws Exception {
        // when & then
        MvcResult result = mockMvc.perform(get("/api/v1/articles")
                        .param("memberId", member.getId().toString())
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
    void 첫번째_페이지_아티클_목록_조회() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/articles")
                        .param("memberId", member.getId().toString())
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
    void 두번째_페이지_아티클_목록_조회() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/articles")
                        .param("memberId", member.getId().toString())
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
    void 날짜_필터링_아티클_목록_조회() throws Exception {
        // given
        LocalDate baseDate = LocalDate.of(2025, 7, 15);

        // when & then
        mockMvc.perform(get("/api/v1/articles")
                        .param("memberId", member.getId().toString())
                        .param("date", baseDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(3)); // 하루 전 제외하고 3개
    }

    @Test
    void 카테고리_키워드_날짜_복합_필터링_아티클_목록_조회() throws Exception {
        // given
        String foodCategory = categories.get(2).getName(); // "푸드"
        LocalDate baseDate = LocalDate.of(2025, 7, 15);

        // when & then
        mockMvc.perform(get("/api/v1/articles")
                        .param("memberId", member.getId().toString())
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
    void memberId_파라미터_누락시_예외() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/articles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 음수_memberId로_조회시_예외() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/articles")
                        .param("memberId", "-1"))
                .andExpect(status().isBadRequest());
    }
}
