package me.bombom.api.v1.coupon.controller;

import static me.bombom.support.acceptance.AcceptanceTestHeaders.MEMBER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.Map;
import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.AdditionalAcceptanceDataSet;
import org.junit.jupiter.api.Test;

@AcceptanceTest("acceptance/common/member.json")
class CouponControllerTest {

    private static final long MEMBER_ID_VALUE = 1L;

    @Test
    @AdditionalAcceptanceDataSet("acceptance/coupon/issued-coupons.json")
    void 내가_받은_쿠폰_목록을_조회한다() {
        List<Map<String, Object>> response = getIssuedCoupons(MEMBER_ID_VALUE);

        assertSoftly(softly -> {
            softly.assertThat(response).hasSize(2);
            softly.assertThat(response)
                    .extracting(coupon -> coupon.get("couponName"))
                    .containsExactlyInAnyOrder("쿠폰A", "쿠폰B");
            softly.assertThat(response)
                    .extracting(coupon -> coupon.get("imageUrl"))
                    .containsExactlyInAnyOrder(
                            "https://cdn.bombom.me/coupon-a.png",
                            "https://cdn.bombom.me/coupon-b.png"
                    );
            softly.assertThat(response)
                    .allSatisfy(coupon -> assertThat(coupon.get("issuedAt")).isNotNull());
        });
    }

    @Test
    void 내가_받은_쿠폰이_없으면_빈_배열을_반환한다() {
        List<Map<String, Object>> response = getIssuedCoupons(MEMBER_ID_VALUE);

        assertThat(response).isEmpty();
    }

    private static List<Map<String, Object>> getIssuedCoupons(long memberId) {
        return RestAssured.given()
                .accept(ContentType.JSON)
                .header(MEMBER_ID, memberId)
                .when()
                .get("/api/v1/coupons/issues/me")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getList("$");
    }
}
