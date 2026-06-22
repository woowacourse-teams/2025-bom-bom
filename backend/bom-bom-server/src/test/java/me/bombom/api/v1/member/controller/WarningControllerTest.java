package me.bombom.api.v1.member.controller;

import static me.bombom.support.acceptance.AcceptanceTestHeaders.MEMBER_ID;
import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.ResetsAcceptanceData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@AcceptanceTest({
        "acceptance/common/member.json",
        "acceptance/member/warning-setting.json"
})
class WarningControllerTest {

    private static final long MEMBER_ID_VALUE = 1L;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void 임박_경고_설정을_조회한다() {
        Map<String, Object> result = RestAssured.given()
                .accept(ContentType.JSON)
                .header(MEMBER_ID, MEMBER_ID_VALUE)
                .when()
                .get("/api/v1/members/me/warning/near-capacity")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");

        assertThat(result.get("isVisible")).isEqualTo(true);
    }

    @Test
    @ResetsAcceptanceData
    void 임박_경고_설정을_수정한다() {
        RestAssured.given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header(MEMBER_ID, MEMBER_ID_VALUE)
                .body(Map.of("isVisible", false))
                .when()
                .post("/api/v1/members/me/warning/near-capacity")
                .then()
                .statusCode(204);

        Boolean visible = jdbcTemplate.queryForObject(
                "select is_visible from warning_setting where member_id = ?",
                Boolean.class,
                MEMBER_ID_VALUE
        );
        assertThat(visible).isFalse();
    }
}
