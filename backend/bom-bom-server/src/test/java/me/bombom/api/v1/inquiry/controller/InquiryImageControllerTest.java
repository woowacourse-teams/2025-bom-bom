package me.bombom.api.v1.inquiry.controller;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.Map;
import me.bombom.api.v1.inquiry.resolver.GuestIdArgumentResolver;
import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.AcceptanceTestHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.unit.DataSize;

@AcceptanceTest("acceptance/inquiry/upload-images.json")
class InquiryImageControllerTest {

    private static final long MEMBER_ROOM_ID = 1L;
    private static final long GUEST_ROOM_ID = 2L;
    private static final long OTHER_MEMBER_ROOM_ID = 3L;
    private static final String GUEST_ID = "e4560796-3db9-4f26-828a-c356c91d5076";
    private static final String OTHER_GUEST_ID = "11111111-2222-4333-8444-555555555555";

    @Value("${spring.servlet.multipart.max-file-size:1MB}")
    private DataSize maxFileSize;

    @Test
    void 이미지_한_장을_업로드한다() {
        Map<String, Object> result = RestAssured.given()
                .accept(ContentType.JSON)
                .header(AcceptanceTestHeaders.MEMBER_ID, 1)
                .multiPart("roomId", MEMBER_ROOM_ID)
                .multiPart("images", "photo.png", "content".getBytes(), "image/png")
                .when()
                .post("/api/v1/inquiries/images")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getMap("$");

        List<String> imageUrls = imageUrls(result);
        assertThat(imageUrls).hasSize(1);
        assertThat(imageUrls.getFirst()).contains("inquiry/");
    }

    @Test
    void 이미지_여러_장을_한번에_업로드한다() {
        Map<String, Object> result = RestAssured.given()
                .accept(ContentType.JSON)
                .header(AcceptanceTestHeaders.MEMBER_ID, 1)
                .multiPart("roomId", MEMBER_ROOM_ID)
                .multiPart("images", "photo1.png", "content1".getBytes(), "image/png")
                .multiPart("images", "photo2.png", "content2".getBytes(), "image/png")
                .when()
                .post("/api/v1/inquiries/images")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getMap("$");

        assertThat(imageUrls(result)).hasSize(2);
    }

    @Test
    void 비회원이_자신의_문의방에_이미지를_업로드한다() {
        Map<String, Object> result = RestAssured.given()
                .accept(ContentType.JSON)
                .header(GuestIdArgumentResolver.GUEST_ID_HEADER, GUEST_ID)
                .multiPart("roomId", GUEST_ROOM_ID)
                .multiPart("images", "photo.png", "content".getBytes(), "image/png")
                .when()
                .post("/api/v1/inquiries/images")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getMap("$");

        assertThat(imageUrls(result)).hasSize(1);
    }

    @Test
    void 다른_회원의_문의방에는_이미지를_업로드할_수_없다() {
        RestAssured.given()
                .accept(ContentType.JSON)
                .header(AcceptanceTestHeaders.MEMBER_ID, 1)
                .multiPart("roomId", OTHER_MEMBER_ROOM_ID)
                .multiPart("images", "photo.png", "content".getBytes(), "image/png")
                .when()
                .post("/api/v1/inquiries/images")
                .then()
                .statusCode(403);
    }

    @Test
    void 다른_비회원의_문의방에는_이미지를_업로드할_수_없다() {
        RestAssured.given()
                .accept(ContentType.JSON)
                .header(GuestIdArgumentResolver.GUEST_ID_HEADER, OTHER_GUEST_ID)
                .multiPart("roomId", GUEST_ROOM_ID)
                .multiPart("images", "photo.png", "content".getBytes(), "image/png")
                .when()
                .post("/api/v1/inquiries/images")
                .then()
                .statusCode(403);
    }

    @Test
    void 회원은_비회원의_문의방에_이미지를_업로드할_수_없다() {
        RestAssured.given()
                .accept(ContentType.JSON)
                .header(AcceptanceTestHeaders.MEMBER_ID, 1)
                .multiPart("roomId", GUEST_ROOM_ID)
                .multiPart("images", "photo.png", "content".getBytes(), "image/png")
                .when()
                .post("/api/v1/inquiries/images")
                .then()
                .statusCode(403);
    }

    @Test
    void 존재하지_않는_문의방이면_거부한다() {
        RestAssured.given()
                .accept(ContentType.JSON)
                .header(AcceptanceTestHeaders.MEMBER_ID, 1)
                .multiPart("roomId", 9999L)
                .multiPart("images", "photo.png", "content".getBytes(), "image/png")
                .when()
                .post("/api/v1/inquiries/images")
                .then()
                .statusCode(404);
    }

    @Test
    void 요청자를_식별할_수_없으면_거부한다() {
        RestAssured.given()
                .accept(ContentType.JSON)
                .multiPart("roomId", MEMBER_ROOM_ID)
                .multiPart("images", "photo.png", "content".getBytes(), "image/png")
                .when()
                .post("/api/v1/inquiries/images")
                .then()
                .statusCode(400);
    }

    @Test
    void 이미지가_4장을_초과하면_거부한다() {
        RestAssured.given()
                .accept(ContentType.JSON)
                .header(AcceptanceTestHeaders.MEMBER_ID, 1)
                .multiPart("roomId", MEMBER_ROOM_ID)
                .multiPart("images", "1.png", "1".getBytes(), "image/png")
                .multiPart("images", "2.png", "2".getBytes(), "image/png")
                .multiPart("images", "3.png", "3".getBytes(), "image/png")
                .multiPart("images", "4.png", "4".getBytes(), "image/png")
                .multiPart("images", "5.png", "5".getBytes(), "image/png")
                .when()
                .post("/api/v1/inquiries/images")
                .then()
                .statusCode(400);
    }

    @Test
    void 파일_크기_제한을_초과하면_거부한다() {
        byte[] oversizedContent = new byte[(int) maxFileSize.toBytes() + 1];

        RestAssured.given()
                .accept(ContentType.JSON)
                .header(AcceptanceTestHeaders.MEMBER_ID, 1)
                .multiPart("roomId", MEMBER_ROOM_ID)
                .multiPart("images", "big.png", oversizedContent, "image/png")
                .when()
                .post("/api/v1/inquiries/images")
                .then()
                .statusCode(413);
    }

    @SuppressWarnings("unchecked")
    private static List<String> imageUrls(Map<String, Object> result) {
        return (List<String>) result.get("imageUrls");
    }
}
