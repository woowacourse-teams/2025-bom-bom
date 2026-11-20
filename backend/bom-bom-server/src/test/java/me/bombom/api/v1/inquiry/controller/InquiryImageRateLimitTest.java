package me.bombom.api.v1.inquiry.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import me.bombom.api.v1.inquiry.resolver.GuestIdArgumentResolver;
import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.AcceptanceTestHeaders;
import me.bombom.support.acceptance.ResetsAcceptanceData;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

@AcceptanceTest("acceptance/inquiry/upload-images-rate-limit.json")
@TestPropertySource(properties = {
        "bom-bom.rate-limit.image-upload.member.bucket-capacity=4",
        "bom-bom.rate-limit.image-upload.member.refill-seconds=1800",
        "bom-bom.rate-limit.image-upload.guest.bucket-capacity=2",
        "bom-bom.rate-limit.image-upload.guest.refill-seconds=3600"
})
class InquiryImageRateLimitTest {

    private static final long MEMBER_ROOM_ID = 1L;
    private static final long GUEST_ROOM_ID = 2L;
    private static final String GUEST_ID = "e4560796-3db9-4f26-828a-c356c91d5076";

    @Test
    @ResetsAcceptanceData
    void 남은_토큰보다_많은_이미지를_요청하면_거부한다() {
        memberRequest(3).post("/api/v1/inquiries/images").then().statusCode(200);

        memberRequest(2)
                .post("/api/v1/inquiries/images")
                .then()
                .statusCode(429);
    }

    @Test
    @ResetsAcceptanceData
    void 토큰을_모두_소진하면_거부한다() {
        memberRequest(4).post("/api/v1/inquiries/images").then().statusCode(200);

        memberRequest(1)
                .post("/api/v1/inquiries/images")
                .then()
                .statusCode(429);
    }

    @Test
    @ResetsAcceptanceData
    void 거부된_요청은_토큰을_차감하지_않는다() {
        memberRequest(3).post("/api/v1/inquiries/images").then().statusCode(200);
        memberRequest(2).post("/api/v1/inquiries/images").then().statusCode(429);

        memberRequest(1)
                .post("/api/v1/inquiries/images")
                .then()
                .statusCode(200);
    }

    @Test
    @ResetsAcceptanceData
    void 비회원은_회원보다_적은_토큰을_가진다() {
        guestRequest(2).post("/api/v1/inquiries/images").then().statusCode(200);

        guestRequest(1)
                .post("/api/v1/inquiries/images")
                .then()
                .statusCode(429);
    }

    @Test
    @ResetsAcceptanceData
    void 회원과_비회원의_토큰은_서로_영향을_주지_않는다() {
        guestRequest(2).post("/api/v1/inquiries/images").then().statusCode(200);

        memberRequest(4)
                .post("/api/v1/inquiries/images")
                .then()
                .statusCode(200);
    }

    private RequestSpecification memberRequest(int imageCount) {
        return imageRequest(imageCount, MEMBER_ROOM_ID)
                .header(AcceptanceTestHeaders.MEMBER_ID, 1);
    }

    private RequestSpecification guestRequest(int imageCount) {
        return imageRequest(imageCount, GUEST_ROOM_ID)
                .header(GuestIdArgumentResolver.GUEST_ID_HEADER, GUEST_ID);
    }

    private RequestSpecification imageRequest(int imageCount, long roomId) {
        RequestSpecification request = RestAssured.given()
                .accept(ContentType.JSON)
                .multiPart("roomId", roomId);

        for (int i = 0; i < imageCount; i++) {
            request.multiPart("images", i + ".png", "content".getBytes(), "image/png");
        }
        return request.when();
    }
}
