package me.bombom.api.v1.pet.controller;

import static me.bombom.support.acceptance.AcceptanceTestHeaders.MEMBER_ID;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.ResetsAcceptanceData;
import org.junit.jupiter.api.Test;

@AcceptanceTest({
        "acceptance/common/member.json",
        "acceptance/pet/pet.json"
})
class PetControllerTest {

    private static final long MEMBER_ID_VALUE = 1L;

    @Test
    void 인증된_사용자는_펫_정보를_조회할_수_있다() {
        Map<String, Object> response = getPet();

        assertSoftly(softly -> {
            softly.assertThat(response.get("level")).isEqualTo(1);
            softly.assertThat(response.get("currentStageScore")).isEqualTo(10);
            softly.assertThat(response.get("requiredStageScore")).isEqualTo(50);
            softly.assertThat(response.get("isAttended")).isEqualTo(false);
        });
    }

    @Test
    @ResetsAcceptanceData
    void 인증된_사용자는_펫_출석_점수를_받을_수_있다() {
        RestAssured.given()
                .accept(ContentType.JSON)
                .header(MEMBER_ID, MEMBER_ID_VALUE)
                .when()
                .post("/api/v1/members/me/pet/attendance")
                .then()
                .statusCode(200);

        Map<String, Object> response = getPet();
        assertSoftly(softly -> {
            softly.assertThat(response.get("currentStageScore")).isEqualTo(15);
            softly.assertThat(response.get("isAttended")).isEqualTo(true);
        });
    }

    @Test
    void 인증되지_않은_사용자는_펫_정보를_조회할_수_없다() {
        RestAssured.given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/members/me/pet")
                .then()
                .statusCode(401);
    }

    private static Map<String, Object> getPet() {
        return RestAssured.given()
                .accept(ContentType.JSON)
                .header(MEMBER_ID, MEMBER_ID_VALUE)
                .when()
                .get("/api/v1/members/me/pet")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");
    }
}
