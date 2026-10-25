package me.bombom.api.v1.member.controller;

import static me.bombom.support.acceptance.AcceptanceTestHeaders.MEMBER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.ResetsAcceptanceData;
import org.junit.jupiter.api.Test;

@AcceptanceTest({
        "acceptance/common/member.json",
        "acceptance/member/member.json"
})
class MemberControllerTest {

    private static final long MEMBER_ID_VALUE = 1L;

    @Test
    void 애플_임시_쿠키는_회원_API_인증에_사용할_수_없다() {
        RestAssured.given().cookie("__Host-BOMBOM_APPLE_AUTH", "A".repeat(43))
                .when().get("/api/v1/members/me").then().statusCode(401);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {
            "https://evil.example", "https://dev.bombom.news.evil.example", "null", "https://appleid.apple.com"})
    void 외부_Origin의_인증된_회원_조회는_차단한다(String origin) {
        var response = RestAssured.given()
                .header(MEMBER_ID, MEMBER_ID_VALUE)
                .header("Origin", origin)
                .when().get("/api/v1/members/me")
                .then().statusCode(403).extract().response();

        assertThat(response.header("Access-Control-Allow-Origin")).isNull();
        assertThat(response.body().asString()).doesNotContain("acceptance@bombom.news");
    }

    @Test
    void 허용된_Origin의_인증된_회원_조회는_허용한다() {
        var response = RestAssured.given()
                .header(MEMBER_ID, MEMBER_ID_VALUE)
                .header("Origin", "http://localhost:3000")
                .when().get("/api/v1/members/me")
                .then().statusCode(200).extract().response();

        assertThat(response.header("Access-Control-Allow-Origin")).isEqualTo("http://localhost:3000");
        assertThat(response.header("Access-Control-Allow-Credentials")).isEqualTo("true");
    }

    @Test
    void 허용된_Origin의_CSRF_헤더_사전_요청은_통과한다() {
        RestAssured.given().header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "PATCH")
                .header("Access-Control-Request-Headers", "Content-Type,X-CSRF-TOKEN")
                .options("/api/v1/members/me").then().statusCode(200)
                .header("Access-Control-Allow-Origin", "http://localhost:3000")
                .header("Access-Control-Allow-Credentials", "true");
    }

    @Test
    void 형식에_맞지_않는_닉네임으로_변경_시도_시_400_예외가_발생한다() {
        updateMember("invalid..nickname")
                .then()
                .statusCode(400);
    }

    @Test
    void 너무_짧은_닉네임으로_변경_시도_시_400_예외가_발생한다() {
        updateMember("a")
                .then()
                .statusCode(400);
    }

    @Test
    @ResetsAcceptanceData
    void 형식에_맞는_닉네임이면_정상_동작한다() {
        Map<String, Object> result = updateMember("new.nickname")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");

        assertThat(result.get("nickname")).isEqualTo("new.nickname");
    }

    @Test
    void 회원_정보를_조회한다() {
        Map<String, Object> result = getMember("/api/v1/members/me");

        assertSoftly(softly -> {
            softly.assertThat(result.get("nickname")).isEqualTo("인수테스트회원");
            softly.assertThat(result.get("email")).isEqualTo("acceptance@bombom.news");
        });
    }

    @Test
    void 회원_프로필을_조회한다() {
        Map<String, Object> result = getMember("/api/v1/members/me/profile");

        assertThat(result.get("nickname")).isEqualTo("인수테스트회원");
    }

    @Test
    void 이미_사용중인_닉네임으로_변경하면_예외가_발생한다() {
        updateMember("duplicated")
                .then()
                .statusCode(400);
    }

    private static io.restassured.response.Response updateMember(String nickname) {
        return RestAssured.given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header(MEMBER_ID, MEMBER_ID_VALUE)
                .body(Map.of("nickname", nickname))
                .when()
                .patch("/api/v1/members/me");
    }

    private static Map<String, Object> getMember(String path) {
        return RestAssured.given()
                .accept(ContentType.JSON)
                .header(MEMBER_ID, MEMBER_ID_VALUE)
                .when()
                .get(path)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");
    }
}
