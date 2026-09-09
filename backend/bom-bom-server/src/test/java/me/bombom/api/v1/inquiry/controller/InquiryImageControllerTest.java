package me.bombom.api.v1.inquiry.controller;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.Map;
import me.bombom.support.acceptance.AcceptanceTest;
import org.junit.jupiter.api.Test;

@AcceptanceTest("acceptance/inquiry/upload-images.json")
class InquiryImageControllerTest {

    @Test
    void 이미지_한_장을_업로드한다() {
        Map<String, Object> result = RestAssured.given()
                .accept(ContentType.JSON)
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
    void 이미지가_4장을_초과하면_거부한다() {
        RestAssured.given()
                .accept(ContentType.JSON)
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

    @SuppressWarnings("unchecked")
    private static List<String> imageUrls(Map<String, Object> result) {
        return (List<String>) result.get("imageUrls");
    }
}
