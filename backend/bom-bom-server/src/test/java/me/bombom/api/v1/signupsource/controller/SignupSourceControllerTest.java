package me.bombom.api.v1.signupsource.controller;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.util.HashMap;
import java.util.Map;
import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.AcceptanceTestHeaders;
import me.bombom.support.acceptance.ResetsAcceptanceData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@AcceptanceTest("acceptance/signupsource/signup-source.json")
class SignupSourceControllerTest {

    private static final long NEW_MEMBER_ID = 1L;
    private static final long ANSWERED_MEMBER_ID = 2L;
    private static final long OLD_MEMBER_ID = 3L;
    private static final String STATUS_PATH = "/api/v1/members/me/signup-source";
    private static final String RECOMMENDATION_PATH = "/api/v1/newsletters/signup-recommendations";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void 가입_경로를_답하지_않은_새_회원이면_질문을_띄운다() {
        given(NEW_MEMBER_ID).get(STATUS_PATH)
                .then()
                .statusCode(200)
                .body("needsSignupSource", equalTo(true));
    }

    @Test
    void 이미_답한_회원이면_질문을_띄우지_않는다() {
        given(ANSWERED_MEMBER_ID).get(STATUS_PATH)
                .then()
                .statusCode(200)
                .body("needsSignupSource", equalTo(false));
    }

    @Test
    void 기능_배포_전에_가입한_회원이면_질문을_띄우지_않는다() {
        given(OLD_MEMBER_ID).get(STATUS_PATH)
                .then()
                .statusCode(200)
                .body("needsSignupSource", equalTo(false));
    }

    @Test
    void 로그인하지_않으면_질문_여부를_조회할_수_없다() {
        given(null).get(STATUS_PATH).then().statusCode(401);
    }

    @Test
    @ResetsAcceptanceData
    void 가입_경로를_저장하면_더_이상_질문을_띄우지_않는다() {
        given(NEW_MEMBER_ID).body(body("YOUTUBE", null)).post(STATUS_PATH)
                .then()
                .statusCode(204);

        assertSoftly(softly -> {
            softly.assertThat(queryForString("SELECT source FROM member_signup_source WHERE member_id = ?", NEW_MEMBER_ID))
                    .isEqualTo("YOUTUBE");
            softly.assertThat(queryForString("SELECT detail FROM member_signup_source WHERE member_id = ?", NEW_MEMBER_ID))
                    .isNull();
        });
        given(NEW_MEMBER_ID).get(STATUS_PATH)
                .then()
                .body("needsSignupSource", equalTo(false));
    }

    @Test
    @ResetsAcceptanceData
    void 기타를_고르면_직접_입력한_경로를_앞뒤_공백_없이_저장한다() {
        given(NEW_MEMBER_ID).body(body("ETC", "  학교 커뮤니티 ")).post(STATUS_PATH)
                .then()
                .statusCode(204);

        assertSoftly(softly -> softly.assertThat(
                queryForString("SELECT detail FROM member_signup_source WHERE member_id = ?", NEW_MEMBER_ID)
        ).isEqualTo("학교 커뮤니티"));
    }

    @Test
    void 기타를_고르고_입력하지_않으면_400을_반환한다() {
        given(NEW_MEMBER_ID).body(body("ETC", "  ")).post(STATUS_PATH)
                .then()
                .statusCode(400)
                .body("code", equalTo("M001"));
    }

    @Test
    void 기타_입력이_50자를_넘으면_400을_반환한다() {
        given(NEW_MEMBER_ID).body(body("ETC", "가".repeat(51))).post(STATUS_PATH)
                .then()
                .statusCode(400);
    }

    @Test
    void 없는_경로_값이면_400을_반환한다() {
        given(NEW_MEMBER_ID).body(body("TIKTOK", null)).post(STATUS_PATH)
                .then()
                .statusCode(400);
    }

    @Test
    void 이미_답한_회원이_다시_저장해도_처음_값을_유지한다() {
        given(ANSWERED_MEMBER_ID).body(body("SEARCH", null)).post(STATUS_PATH)
                .then()
                .statusCode(204);

        assertSoftly(softly -> softly.assertThat(
                queryForString("SELECT source FROM member_signup_source WHERE member_id = ?", ANSWERED_MEMBER_ID)
        ).isEqualTo("THREADS"));
    }

    @Test
    void 로그인하지_않으면_가입_경로를_저장할_수_없다() {
        given(null).body(body("YOUTUBE", null)).post(STATUS_PATH).then().statusCode(401);
    }

    @Test
    void 추천_목록은_추천_순서대로_활성_뉴스레터만_구독자_수와_함께_반환한다() {
        given(null).get(RECOMMENDATION_PATH)
                .then()
                .statusCode(200)
                .body("newsletters.name", contains("주말토리", "까탈로그", "캐릿"))
                .body("newsletters.subscriberCount", contains(1, 2, 0))
                .body("newsletters.arrivalDays", contains("매주 금요일", "매주 금요일", "매주 화요일"))
                .body("newsletters[0].subscribeUrl", equalTo("https://page.stibee.com/subscriptions/1"))
                .body("newsletters[0].category", equalTo("지역/여행"))
                .body("categories", contains("트렌드/라이프", "지역/여행"));
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

    private Map<String, Object> body(String source, String detail) {
        Map<String, Object> body = new HashMap<>();
        body.put("source", source);
        body.put("detail", detail);
        return body;
    }

    private String queryForString(String sql, Object... args) {
        return jdbcTemplate.queryForObject(sql, String.class, args);
    }
}
