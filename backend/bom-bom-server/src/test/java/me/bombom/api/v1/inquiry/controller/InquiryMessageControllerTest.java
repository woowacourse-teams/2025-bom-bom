package me.bombom.api.v1.inquiry.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.Map;
import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.AcceptanceTestHeaders;
import me.bombom.support.acceptance.ResetsAcceptanceData;
import org.junit.jupiter.api.Test;

@AcceptanceTest("acceptance/inquiry/messages.json")
class InquiryMessageControllerTest {

    @Test
    @ResetsAcceptanceData
    void 본인_채팅방에_메시지를_전송한다() {
        Map<String, Object> result = sendMessage(1, "문의합니다", List.of())
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getMap("$");

        assertSoftly(softly -> {
            softly.assertThat(result.get("content")).isEqualTo("문의합니다");
            softly.assertThat(result.get("senderType")).isEqualTo("USER");
            softly.assertThat(result.get("adminId")).isNull();
        });
    }

    @Test
    @ResetsAcceptanceData
    void 이미지를_포함해_메시지를_전송한다() {
        List<String> imageUrls = List.of("https://s3/img1.png", "https://s3/img2.png");

        Map<String, Object> result = sendMessage(1, "사진 첨부합니다", imageUrls)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getMap("$");

        assertThat((List<String>) result.get("imageUrls")).containsExactlyElementsOf(imageUrls);
    }

    @Test
    void 이미지가_4장을_초과하면_거부한다() {
        List<String> imageUrls = List.of("1", "2", "3", "4", "5");

        sendMessage(1, "많은 사진", imageUrls)
                .then()
                .statusCode(400);
    }

    @Test
    void 내용이_비어있으면_거부한다() {
        sendMessage(1, "", List.of())
                .then()
                .statusCode(400);
    }

    @Test
    void 다른_사람의_채팅방에는_메시지를_보낼_수_없다() {
        sendMessage(2, "몰래 문의", List.of())
                .then()
                .statusCode(403);
    }

    @Test
    void 존재하지_않는_채팅방에는_메시지를_보낼_수_없다() {
        sendMessage(999, "없는 방", List.of())
                .then()
                .statusCode(404);
    }

    @Test
    @ResetsAcceptanceData
    void 비회원이_본인_채팅방에_메시지를_전송한다() {
        Map<String, Object> result = RestAssured.given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("X-Guest-Id", "guest-uuid-1")
                .body(Map.of("content", "비회원 문의", "imageUrls", List.of()))
                .when()
                .post("/api/v1/inquiries/rooms/{roomId}/messages", 2)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getMap("$");

        assertThat(result.get("content")).isEqualTo("비회원 문의");
    }

    @Test
    @ResetsAcceptanceData
    void 커서_없이_조회하면_최신_메시지부터_내려온다() {
        sendMessage(1, "메시지1", List.of()).then().statusCode(200);
        sendMessage(1, "메시지2", List.of()).then().statusCode(200);
        sendMessage(1, "메시지3", List.of()).then().statusCode(200);

        Map<String, Object> result = getMessages(1, null, 20);

        List<Map<String, Object>> messages = messages(result);
        assertSoftly(softly -> {
            softly.assertThat(messages).hasSize(3);
            softly.assertThat(messages.get(0).get("content")).isEqualTo("메시지3");
            softly.assertThat(result.get("hasNext")).isEqualTo(false);
        });
    }

    @Test
    @ResetsAcceptanceData
    void size보다_많은_메시지가_있으면_hasNext가_true다() {
        for (int i = 0; i < 3; i++) {
            sendMessage(1, "메시지" + i, List.of()).then().statusCode(200);
        }

        Map<String, Object> result = getMessages(1, null, 2);

        List<Map<String, Object>> messages = messages(result);
        assertSoftly(softly -> {
            softly.assertThat(messages).hasSize(2);
            softly.assertThat(result.get("hasNext")).isEqualTo(true);
        });
    }

    @Test
    @ResetsAcceptanceData
    void 커서_이전_메시지만_조회한다() {
        sendMessage(1, "메시지1", List.of()).then().statusCode(200);
        Map<String, Object> second = sendMessage(1, "메시지2", List.of())
                .then().statusCode(200).extract().jsonPath().getMap("$");
        sendMessage(1, "메시지3", List.of()).then().statusCode(200);

        long cursor = ((Number) second.get("id")).longValue();
        Map<String, Object> result = getMessages(1, cursor, 20);

        List<Map<String, Object>> messages = messages(result);
        assertSoftly(softly -> {
            softly.assertThat(messages).hasSize(1);
            softly.assertThat(messages.get(0).get("content")).isEqualTo("메시지1");
        });
    }

    @Test
    void 다른_사람의_채팅방_메시지는_조회할_수_없다() {
        getMessagesResponse(2, null, 20)
                .then()
                .statusCode(403);
    }

    @Test
    void 존재하지_않는_채팅방의_메시지는_조회할_수_없다() {
        getMessagesResponse(999, null, 20)
                .then()
                .statusCode(404);
    }

    private static io.restassured.response.Response sendMessage(long roomId, String content, List<String> imageUrls) {
        return RestAssured.given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header(AcceptanceTestHeaders.MEMBER_ID, 1)
                .body(Map.of("content", content, "imageUrls", imageUrls))
                .when()
                .post("/api/v1/inquiries/rooms/{roomId}/messages", roomId);
    }

    private static Map<String, Object> getMessages(long roomId, Long cursor, int size) {
        return getMessagesResponse(roomId, cursor, size)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getMap("$");
    }

    private static io.restassured.response.Response getMessagesResponse(long roomId, Long cursor, int size) {
        io.restassured.specification.RequestSpecification spec = RestAssured.given()
                .accept(ContentType.JSON)
                .header(AcceptanceTestHeaders.MEMBER_ID, 1)
                .queryParam("size", size);
        if (cursor != null) {
            spec = spec.queryParam("cursor", cursor);
        }
        return spec
                .when()
                .get("/api/v1/inquiries/rooms/{roomId}/messages", roomId);
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> messages(Map<String, Object> page) {
        return (List<Map<String, Object>>) page.get("messages");
    }
}
