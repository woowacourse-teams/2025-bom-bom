package me.bombom.api.v1.newsletter.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import me.bombom.support.MutableClock;
import me.bombom.support.acceptance.AdditionalAcceptanceDataSet;
import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.AcceptanceTestHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

@AcceptanceTest("acceptance/newsletter/get-newsletters.json")
class NewsletterControllerTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 6, 21);
    private static final long MEMBER_ID = 1L;

    @Autowired
    private MutableClock clock;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        clock.setDate(TODAY);
    }

    @Test
    void 비로그인_사용자는_전체_뉴스레터를_구독하지_않은_상태로_조회한다() {
        Map<String, Object> result = requestNewsletters(Map.of(), false);
        List<Map<String, Object>> newsletters = newsletters(result);

        assertSoftly(softly -> {
            softly.assertThat(newsletters).hasSize(4);
            softly.assertThat(newsletters).extracting(newsletter -> newsletter.get("isSubscribed"))
                    .containsOnly(false);
            softly.assertThat(newsletters).extracting(newsletter -> newsletter.get("source"))
                    .containsOnly("EXTERNAL");
        });
    }

    @Test
    void 로그인_사용자는_구독_여부와_함께_뉴스레터를_조회한다() {
        Map<String, Object> result = requestNewsletters(Map.of(), true);

        assertThat(newsletters(result)).extracting(newsletter -> newsletter.get("isSubscribed"))
                .contains(true);
    }

    @Test
    void 뉴스레터_상세와_구독_여부를_조회한다() {
        Map<String, Object> result = requestNewsletterDetail(1, true);

        assertSoftly(softly -> {
            softly.assertThat(result.get("name")).isEqualTo("뉴스픽");
            softly.assertThat(result.get("description")).isEqualTo("설명");
            softly.assertThat(result.get("isSubscribed")).isEqualTo(true);
        });
    }

    @Test
    void 존재하지_않는_뉴스레터_상세는_404를_반환한다() {
        RestAssuredMockMvc.given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/newsletters/{id}", 99999)
                .then()
                .statusCode(404);
    }

    @Test
    void includeSuspended가_false이면_휴재와_폐간을_제외한다() {
        Map<String, Object> result = requestNewsletters(Map.of(), false);

        assertThat(newsletters(result)).extracting(newsletter -> newsletter.get("newsletterId"))
                .doesNotContain(5, 6);
    }

    @Test
    void includeSuspended가_true이면_최근_휴재만_포함한다() {
        Map<String, Object> result = requestNewsletters(Map.of("includeSuspended", true), false);

        assertThat(newsletters(result)).extracting(newsletter -> newsletter.get("newsletterId"))
                .contains(7)
                .doesNotContain(8);
    }

    @Test
    void categoryId로_뉴스레터를_필터링한다() {
        Map<String, Object> result = requestNewsletters(Map.of("categoryId", 3), false);
        List<Map<String, Object>> newsletters = newsletters(result);

        assertSoftly(softly -> {
            softly.assertThat(newsletters).hasSize(2);
            softly.assertThat(newsletters).extracting(newsletter -> newsletter.get("categoryId"))
                    .containsOnly(3);
        });
    }

    @Test
    @AdditionalAcceptanceDataSet("acceptance/newsletter/additional-without-count.json")
    void 구독자_수_집계가_없는_뉴스레터도_조회한다() {
        Map<String, Object> result = requestNewsletters(Map.of(), false);

        assertThat(newsletters(result)).extracting(newsletter -> newsletter.get("newsletterId"))
                .contains(9);
    }

    @Test
    void 유효하지_않은_인증_객체여도_공개_목록을_반환한다() throws Exception {
        TestingAuthenticationToken invalidAuthentication = new TestingAuthenticationToken("invalid", null);
        invalidAuthentication.setAuthenticated(true);

        mockMvc.perform(get("/api/v1/newsletters").with(authentication(invalidAuthentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newsletters.length()").value(4))
                .andExpect(jsonPath("$.newsletters[*].newsletterId").value(not(hasItem(is(5)))))
                .andExpect(jsonPath("$.newsletters[*].newsletterId").value(not(hasItem(is(6)))))
                .andExpect(jsonPath("$.newsletters[*].newsletterId").value(not(hasItem(is(7)))))
                .andExpect(jsonPath("$.newsletters[*].newsletterId").value(not(hasItem(is(8)))))
                .andExpect(jsonPath("$.newsletters[*].newsletterId").value(not(hasItem(is(9)))));
    }

    private static Map<String, Object> requestNewsletters(Map<String, ?> query, boolean authenticated) {
        var request = RestAssuredMockMvc.given()
                .accept(ContentType.JSON)
                .queryParams(query);
        if (authenticated) {
            request.header(AcceptanceTestHeaders.MEMBER_ID, MEMBER_ID);
        }
        return request.when()
                .get("/api/v1/newsletters")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");
    }

    private static Map<String, Object> requestNewsletterDetail(long newsletterId, boolean authenticated) {
        var request = RestAssuredMockMvc.given()
                .accept(ContentType.JSON);
        if (authenticated) {
            request.header(AcceptanceTestHeaders.MEMBER_ID, MEMBER_ID);
        }
        return request.when()
                .get("/api/v1/newsletters/{id}", newsletterId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> newsletters(Map<String, Object> result) {
        return (List<Map<String, Object>>) result.get("newsletters");
    }
}
