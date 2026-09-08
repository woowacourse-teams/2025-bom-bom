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

    private static io.restassured.response.Response sendMessage(long roomId, String content, List<String> imageUrls) {
        return RestAssured.given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header(AcceptanceTestHeaders.MEMBER_ID, 1)
                .body(Map.of("content", content, "imageUrls", imageUrls))
                .when()
                .post("/api/v1/inquiries/rooms/{roomId}/messages", roomId);
    }
}
