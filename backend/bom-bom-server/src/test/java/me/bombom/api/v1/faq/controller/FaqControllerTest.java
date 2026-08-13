package me.bombom.api.v1.faq.controller;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.Map;
import me.bombom.support.acceptance.AcceptanceTest;
import org.junit.jupiter.api.Test;

@AcceptanceTest("acceptance/faq/get-faqs.json")
class FaqControllerTest {

    @Test
    void FAQ_목록을_생성일_내림차순과_ID_오름차순으로_페이지네이션하여_조회한다() {
        Map<String, Object> result = RestAssured.given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/faqs")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");

        assertSoftly(softly -> {
            softly.assertThat(content(result).get(0).get("question")).isEqualTo("질문3");
            softly.assertThat(content(result).get(1).get("question")).isEqualTo("질문2");
            softly.assertThat(content(result).get(2).get("question")).isEqualTo("질문1");
            softly.assertThat(result.get("totalElements")).isEqualTo(3);
            softly.assertThat(result.get("size")).isEqualTo(20);
            softly.assertThat(sort(result).get("sorted")).isEqualTo(true);
        });
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> content(Map<String, Object> page) {
        return (List<Map<String, Object>>) page.get("content");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> sort(Map<String, Object> page) {
        return (Map<String, Object>) page.get("sort");
    }
}
