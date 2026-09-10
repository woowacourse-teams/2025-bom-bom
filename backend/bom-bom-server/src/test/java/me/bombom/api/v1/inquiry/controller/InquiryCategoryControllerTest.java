package me.bombom.api.v1.inquiry.controller;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.Map;
import me.bombom.support.acceptance.AcceptanceTest;
import org.junit.jupiter.api.Test;

@AcceptanceTest("acceptance/inquiry/get-categories.json")
class InquiryCategoryControllerTest {

    @Test
    void 문의_카테고리_목록을_id_오름차순으로_조회한다() {
        List<Map<String, Object>> result = RestAssured.given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/inquiries/categories")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getList("$");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).get("name")).isEqualTo("뉴스레터");
        assertThat(result.get(1).get("name")).isEqualTo("기타");
    }
}
