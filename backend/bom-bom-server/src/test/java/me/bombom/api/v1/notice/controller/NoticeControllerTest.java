package me.bombom.api.v1.notice.controller;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.Map;
import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.AdditionalAcceptanceDataSet;
import org.junit.jupiter.api.Test;

@AcceptanceTest("acceptance/notice/get-notices.json")
class NoticeControllerTest {

    private static final int PRIVATE_NOTICE_ID = 4;

    @Test
    void 공지_목록을_생성일_내림차순과_ID_오름차순으로_페이지네이션하여_조회한다() {
        Map<String, Object> result = getNotices();

        assertSoftly(softly -> {
            softly.assertThat(content(result).get(0).get("title")).isEqualTo("공지3");
            softly.assertThat(content(result).get(1).get("title")).isEqualTo("공지2");
            softly.assertThat(content(result).get(2).get("title")).isEqualTo("공지1");
            softly.assertThat(result.get("totalElements")).isEqualTo(3);
            softly.assertThat(result.get("size")).isEqualTo(20);
            softly.assertThat(sort(result).get("sorted")).isEqualTo(true);
        });
    }

    @Test
    void 비공개_공지는_목록에서_제외된다() {
        Map<String, Object> result = getNotices();

        assertSoftly(softly -> {
            softly.assertThat(noticeIds(result)).doesNotContain(PRIVATE_NOTICE_ID);
            softly.assertThat(result.get("totalElements")).isEqualTo(3);
        });
    }

    private static Map<String, Object> getNotices() {
        return RestAssured.given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/notices")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");
    }

    private static List<Object> noticeIds(Map<String, Object> page) {
        return content(page).stream()
                .map(notice -> notice.get("noticeId"))
                .toList();
    }

    @Test
    @AdditionalAcceptanceDataSet("acceptance/notice/representative-public-notice.json")
    void 대표로_지정된_공개_공지를_조회한다() {
        Map<String, Object> result = RestAssured.given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/notices/representative")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");

        assertSoftly(softly -> {
            softly.assertThat(result.get("noticeId")).isEqualTo(3);
            softly.assertThat(result.get("title")).isEqualTo("공지3");
        });
    }

    @Test
    void 대표_공지가_없으면_204를_반환한다() {
        RestAssured.given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/notices/representative")
                .then()
                .statusCode(204);
    }

    @Test
    @AdditionalAcceptanceDataSet("acceptance/notice/representative-private-notice.json")
    void 비공개_공지가_대표로_지정돼_있으면_204를_반환한다() {
        RestAssured.given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/notices/representative")
                .then()
                .statusCode(204);
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
