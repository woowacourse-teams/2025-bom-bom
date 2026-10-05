package me.bombom.api.v1.newsletterrequest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.AcceptanceTestHeaders;
import me.bombom.support.acceptance.ResetsAcceptanceData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@AcceptanceTest({
        "acceptance/common/member.json",
        "acceptance/newsletterrequest/newsletter-request.json"
})
class NewsletterRequestControllerTest {

    private static final long MEMBER_ID = 1L;
    private static final long REQUESTER_ID = 2L;
    private static final String BASE_PATH = "/api/v1/newsletter-requests";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @ResetsAcceptanceData
    void 새_링크로_신청하면_신청과_대기_초안을_만든다() {
        Response response = create(MEMBER_ID, body("모닝 디자인 레터", "https://www.morning.design/letter/", "추천해요"));

        response.then().statusCode(200);
        Long id = response.jsonPath().getLong("newsletterRequestId");
        assertSoftly(softly -> {
            softly.assertThat(queryForString("SELECT normalized_url FROM newsletter_request WHERE id = ?", id))
                    .isEqualTo("morning.design/letter");
            softly.assertThat(queryForString("SELECT status FROM newsletter_request WHERE id = ?", id))
                    .isEqualTo("RECEIVED");
            softly.assertThat(queryForString("SELECT reason FROM newsletter_request WHERE id = ?", id))
                    .isEqualTo("추천해요");
            softly.assertThat(queryForInt("SELECT like_count FROM newsletter_request WHERE id = ?", id)).isZero();
            softly.assertThat(queryForString(
                    "SELECT collect_status FROM newsletter_request_draft WHERE newsletter_request_id = ?",
                    id
            )).isEqualTo("PENDING");
        });
    }

    @Test
    void 로그인하지_않으면_신청할_수_없다() {
        create(null, body("모닝 디자인 레터", "https://morning.design", null))
                .then()
                .statusCode(401);
    }

    @Test
    void 진행_중인_신청과_같은_링크로_신청하면_400을_반환한다() {
        create(MEMBER_ID, body("주간 개발 노트", "http://www.weeklydev.stibee.com/", null))
                .then()
                .statusCode(400)
                .body("code", org.hamcrest.Matchers.equalTo("M009"));
    }

    @Test
    void 등록된_뉴스레터와_같은_링크로_신청하면_400을_반환한다() {
        create(MEMBER_ID, body("뉴스픽", "https://registered.com/subscribe?ref=x", null))
                .then()
                .statusCode(400)
                .body("code", org.hamcrest.Matchers.equalTo("M009"));
    }

    @Test
    @ResetsAcceptanceData
    void 반려된_링크로_신청하면_기존_신청을_다시_접수한다() {
        Response response = create(REQUESTER_ID, body("다시 신청", "https://rejected.com", null));

        response.then().statusCode(200);
        assertSoftly(softly -> {
            softly.assertThat(response.jsonPath().getLong("newsletterRequestId")).isEqualTo(2L);
            softly.assertThat(queryForString("SELECT status FROM newsletter_request WHERE id = 2")).isEqualTo("RECEIVED");
            softly.assertThat(queryForInt("SELECT requester_member_id FROM newsletter_request WHERE id = 2"))
                    .isEqualTo((int) REQUESTER_ID);
            softly.assertThat(queryForInt("SELECT like_count FROM newsletter_request WHERE id = 2")).isZero();
            softly.assertThat(queryForInt("SELECT COUNT(*) FROM newsletter_request_like WHERE newsletter_request_id = 2"))
                    .isZero();
            softly.assertThat(queryForString(
                    "SELECT collect_status FROM newsletter_request_draft WHERE newsletter_request_id = 2"
            )).isEqualTo("PENDING");
        });
    }

    @Test
    void 이름이_비어_있으면_400을_반환한다() {
        create(MEMBER_ID, body(" ", "https://new.com", null)).then().statusCode(400);
    }

    @Test
    void 이름이_50자를_넘으면_400을_반환한다() {
        create(MEMBER_ID, body("가".repeat(51), "https://new.com", null)).then().statusCode(400);
    }

    @Test
    void 추천_이유가_200자를_넘으면_400을_반환한다() {
        create(MEMBER_ID, body("새 레터", "https://new.com", "가".repeat(201))).then().statusCode(400);
    }

    @Test
    void 링크_형식이_잘못되면_400을_반환한다() {
        create(MEMBER_ID, body("새 레터", "notaurl", null)).then().statusCode(400);
    }

    @Test
    void 진행_중인_신청과_같은_링크면_REQUESTED를_반환한다() {
        Map<String, Object> result = check("https://www.weeklydev.stibee.com/");

        assertThat(result).containsEntry("result", "REQUESTED")
                .containsEntry("newsletterRequestId", 1);
    }

    @Test
    void 등록된_뉴스레터와_같은_링크면_REGISTERED를_반환한다() {
        Map<String, Object> result = check("registered.com");

        assertThat(result).containsEntry("result", "REGISTERED")
                .containsEntry("newsletterId", 1);
    }

    @Test
    void 새_링크거나_반려된_링크면_AVAILABLE을_반환한다() {
        assertThat(check("new.com")).containsEntry("result", "AVAILABLE");
        assertThat(check("rejected.com")).containsEntry("result", "AVAILABLE");
    }

    @Test
    @ResetsAcceptanceData
    void 좋아요를_누르면_좋아요_수가_늘고_두_번_눌러도_한_번만_반영된다() {
        like(MEMBER_ID, 1L).then().statusCode(200);
        Response response = like(MEMBER_ID, 1L);

        response.then().statusCode(200);
        assertSoftly(softly -> {
            softly.assertThat(response.jsonPath().getInt("likeCount")).isEqualTo(2);
            softly.assertThat(queryForInt("SELECT like_count FROM newsletter_request WHERE id = 1")).isEqualTo(2);
        });
    }

    @Test
    @ResetsAcceptanceData
    void 좋아요를_취소하면_좋아요_수가_줄어든다() {
        Response response = unlike(MEMBER_ID, 4L);

        response.then().statusCode(200);
        assertSoftly(softly -> {
            softly.assertThat(response.jsonPath().getInt("likeCount")).isEqualTo(3);
            softly.assertThat(queryForInt(
                    "SELECT COUNT(*) FROM newsletter_request_like WHERE newsletter_request_id = 4 AND member_id = ?",
                    MEMBER_ID
            )).isZero();
        });
    }

    @Test
    void 좋아요하지_않은_신청을_취소해도_좋아요_수는_그대로다() {
        Response response = unlike(MEMBER_ID, 1L);

        response.then().statusCode(200);
        assertThat(response.jsonPath().getInt("likeCount")).isEqualTo(1);
    }

    @Test
    void 내가_신청한_신청에는_좋아요할_수_없다() {
        like(REQUESTER_ID, 1L).then().statusCode(400);
    }

    @Test
    void 진행_중이_아닌_신청에는_좋아요할_수_없다() {
        like(MEMBER_ID, 3L).then().statusCode(400);
    }

    @Test
    void 없는_신청에_좋아요하면_404를_반환한다() {
        like(MEMBER_ID, 999L).then().statusCode(404);
    }

    @Test
    void 로그인하지_않으면_좋아요할_수_없다() {
        like(null, 1L).then().statusCode(401);
    }

    @Test
    void 보드는_진행_중이거나_최근_등록된_신청을_공감_수_순서로_보여준다() {
        List<Map<String, Object>> result = given(MEMBER_ID).get(BASE_PATH)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("$");

        assertSoftly(softly -> {
            softly.assertThat(result).extracting(item -> item.get("id")).containsExactly(4, 3, 1);
            softly.assertThat(result.get(0))
                    .containsEntry("name", "디자인 레터 위클리")
                    .containsEntry("status", "REVIEWING")
                    .containsEntry("likeCount", 4)
                    .containsEntry("liked", true)
                    .containsEntry("mine", false)
                    .containsEntry("categoryName", "IT/테크")
                    .containsEntry("imageUrl", "https://cdn.bombom.me/design.png");
            softly.assertThat(result.get(2))
                    .containsEntry("name", "주간 개발 노트")
                    .containsEntry("liked", false)
                    .containsEntry("categoryName", null);
            softly.assertThat(result.get(1)).containsEntry("newsletterId", 1);
        });
    }

    @Test
    void 로그인하지_않아도_보드를_조회할_수_있다() {
        List<Map<String, Object>> result = given(null).get(BASE_PATH)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("$");

        assertThat(result).extracting(item -> item.get("liked")).containsOnly(false);
    }

    @Test
    void 내_신청에는_내가_신청했거나_좋아요한_신청이_반려된_것까지_최신순으로_포함된다() {
        List<Map<String, Object>> result = given(MEMBER_ID).get(BASE_PATH + "/me")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("$");

        assertSoftly(softly -> {
            softly.assertThat(result).extracting(item -> item.get("id")).containsExactly(4, 2);
            softly.assertThat(result.get(0))
                    .containsEntry("liked", true)
                    .containsEntry("mine", false);
            softly.assertThat(result.get(1))
                    .containsEntry("status", "REJECTED")
                    .containsEntry("liked", false)
                    .containsEntry("mine", true);
        });
    }

    @Test
    void 로그인하지_않으면_내_신청을_조회할_수_없다() {
        given(null).get(BASE_PATH + "/me").then().statusCode(401);
    }

    @Test
    void 이름으로_진행_중인_신청과_등록된_뉴스레터를_추천한다() {
        Map<String, Object> requests = suggestions("weekly");
        Map<String, Object> newsletters = suggestions("뉴스 픽");

        assertSoftly(softly -> {
            softly.assertThat(list(requests, "requests")).extracting(item -> item.get("id")).containsExactly(1);
            softly.assertThat(list(newsletters, "newsletters"))
                    .extracting(item -> item.get("newsletterId"))
                    .containsExactly(1);
        });
    }

    @Test
    void 검색어가_2자_미만이면_빈_목록을_반환한다() {
        Map<String, Object> result = suggestions("주");

        assertSoftly(softly -> {
            softly.assertThat(list(result, "requests")).isEmpty();
            softly.assertThat(list(result, "newsletters")).isEmpty();
        });
    }

    private RequestSpecification given(Long memberId) {
        RequestSpecification specification = RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
        if (memberId != null) {
            specification.header(AcceptanceTestHeaders.MEMBER_ID, memberId);
        }
        return specification;
    }

    private Map<String, Object> body(String name, String url, String reason) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("url", url);
        body.put("reason", reason);
        body.put("isNotificationEnabled", true);
        return body;
    }

    private Response create(Long memberId, Map<String, Object> body) {
        return given(memberId).body(body).post(BASE_PATH);
    }

    private Map<String, Object> check(String url) {
        return given(null).queryParam("url", url)
                .get(BASE_PATH + "/check")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getMap("$");
    }

    private Response like(Long memberId, Long newsletterRequestId) {
        return given(memberId).put(BASE_PATH + "/" + newsletterRequestId + "/like");
    }

    private Response unlike(Long memberId, Long newsletterRequestId) {
        return given(memberId).delete(BASE_PATH + "/" + newsletterRequestId + "/like");
    }

    private Map<String, Object> suggestions(String keyword) {
        return given(null).queryParam("keyword", keyword)
                .get(BASE_PATH + "/suggestions")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getMap("$");
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> list(Map<String, Object> result, String key) {
        return (List<Map<String, Object>>) result.get(key);
    }

    private String queryForString(String sql, Object... arguments) {
        return jdbcTemplate.queryForObject(sql, String.class, arguments);
    }

    private int queryForInt(String sql, Object... arguments) {
        return jdbcTemplate.queryForObject(sql, Integer.class, arguments);
    }
}
