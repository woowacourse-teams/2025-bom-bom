package me.bombom.api.v1.inquiry.controller;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.Map;
import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.AcceptanceTestHeaders;
import me.bombom.support.acceptance.ResetsAcceptanceData;
import org.junit.jupiter.api.Test;

@AcceptanceTest("acceptance/inquiry/rooms.json")
class InquiryRoomControllerTest {

    @Test
    @ResetsAcceptanceData
    void 회원이_문의_채팅방을_생성한다() {
        Map<String, Object> result = RestAssured.given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header(AcceptanceTestHeaders.MEMBER_ID, 1)
                .body(Map.of("categoryId", 1))
                .when()
                .post("/api/v1/inquiries/rooms")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getMap("$");

        assertSoftly(softly -> {
            softly.assertThat(result.get("categoryId")).isEqualTo(1);
            softly.assertThat(result.get("status")).isEqualTo("UNCONFIRMED");
        });
    }

    @Test
    @ResetsAcceptanceData
    void 비회원이_X_Guest_Id_헤더로_문의_채팅방을_생성한다() {
        Map<String, Object> result = RestAssured.given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("X-Guest-Id", "new-guest-uuid")
                .body(Map.of("categoryId", 1))
                .when()
                .post("/api/v1/inquiries/rooms")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getMap("$");

        assertSoftly(softly -> {
            softly.assertThat(result.get("categoryId")).isEqualTo(1);
            softly.assertThat(result.get("status")).isEqualTo("UNCONFIRMED");
        });
    }

    @Test
    void 회원도_비회원도_아니면_채팅방을_생성할_수_없다() {
        RestAssured.given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(Map.of("categoryId", 1))
                .when()
                .post("/api/v1/inquiries/rooms")
                .then()
                .statusCode(400);
    }

    @Test
    @ResetsAcceptanceData
    void 회원의_채팅방_목록을_최신순으로_조회한다() {
        createRoom(AcceptanceTestHeaders.MEMBER_ID, "1");
        createRoom(AcceptanceTestHeaders.MEMBER_ID, "1");

        Map<String, Object> result = RestAssured.given()
                .accept(ContentType.JSON)
                .header(AcceptanceTestHeaders.MEMBER_ID, 1)
                .when()
                .get("/api/v1/inquiries/rooms")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getMap("$");

        List<Map<String, Object>> content = content(result);
        assertSoftly(softly -> {
            softly.assertThat(result.get("totalElements")).isEqualTo(2);
            softly.assertThat(result.get("size")).isEqualTo(20);
            softly.assertThat(content).hasSize(2);
        });
    }

    @Test
    @ResetsAcceptanceData
    void 비회원의_채팅방_목록을_guestId로_조회한다() {
        createRoom("X-Guest-Id", "guest-uuid-1");

        Map<String, Object> result = RestAssured.given()
                .accept(ContentType.JSON)
                .header("X-Guest-Id", "guest-uuid-1")
                .when()
                .get("/api/v1/inquiries/rooms")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getMap("$");

        assertSoftly(softly -> {
            softly.assertThat(result.get("totalElements")).isEqualTo(1);
            softly.assertThat(content(result)).hasSize(1);
        });
    }

    @Test
    void 회원도_비회원도_아니면_채팅방_목록을_조회할_수_없다() {
        RestAssured.given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/inquiries/rooms")
                .then()
                .statusCode(400);
    }

    private static void createRoom(String headerName, String headerValue) {
        RestAssured.given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header(headerName, headerValue)
                .body(Map.of("categoryId", 1))
                .when()
                .post("/api/v1/inquiries/rooms")
                .then()
                .statusCode(200);
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> content(Map<String, Object> page) {
        return (List<Map<String, Object>>) page.get("content");
    }
}
