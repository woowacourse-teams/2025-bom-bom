package me.bombom.api.v1.subscribe.controller;

import static me.bombom.support.acceptance.AcceptanceTestHeaders.MEMBER_ID;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.Map;
import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.ResetsAcceptanceData;
import org.junit.jupiter.api.Test;

@AcceptanceTest({
        "acceptance/common/member.json",
        "acceptance/subscribe/subscription.json"
})
class SubscribeControllerTest {

    private static final long MEMBER_ID_VALUE = 1L;
    private static final long SUBSCRIPTION_ID = 1L;

    @Test
    void 인증된_사용자는_구독_목록을_조회할_수_있다() {
        List<Map<String, Object>> result = getSubscriptions(MEMBER_ID_VALUE);

        assertSoftly(softly -> {
            softly.assertThat(result.getFirst().get("subscriptionId")).isEqualTo((int) SUBSCRIPTION_ID);
            softly.assertThat(result.getFirst().get("name")).isEqualTo("테스트 뉴스레터");
        });
    }

    @Test
    void 인증되지_않은_사용자는_401을_반환한다() {
        RestAssured.given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/members/me/subscriptions")
                .then()
                .statusCode(401);
    }

    @Test
    @ResetsAcceptanceData
    void 인증된_사용자는_구독_취소를_요청할_수_있다() {
        RestAssured.given()
                .accept(ContentType.JSON)
                .header(MEMBER_ID, MEMBER_ID_VALUE)
                .when()
                .post("/api/v1/members/me/subscriptions/{id}/unsubscribe", SUBSCRIPTION_ID)
                .then()
                .statusCode(200);
    }

    private static List<Map<String, Object>> getSubscriptions(long memberId) {
        return RestAssured.given()
                .accept(ContentType.JSON)
                .header(MEMBER_ID, memberId)
                .when()
                .get("/api/v1/members/me/subscriptions")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getList("$");
    }
}
