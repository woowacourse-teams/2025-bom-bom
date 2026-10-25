package me.bombom.api.v1.auth.controller;

import static me.bombom.support.acceptance.AcceptanceTestHeaders.MEMBER_ID;
import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Map;
import me.bombom.api.v1.auth.dto.PendingOAuth2Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.ResetsAcceptanceData;
import me.bombom.support.auth.FakeAppleOAuth2Service;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@AcceptanceTest({"acceptance/common/member.json", "acceptance/member/member.json"})
@TestPropertySource(properties = "app.security.csrf-enabled=true")
class CsrfControllerTest {

    @Autowired
    private FakeAppleOAuth2Service appleService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 네이티브_로그인도_이전_세션과_CSRF를_폐기한다() {
        appleService.allowNativeLogin(memberRepository.findById(1L).orElseThrow());
        Response before = csrf();
        String sessionCookie = before.getCookies().keySet().iterator().next();
        Response login = RestAssured.given().cookies(before.getCookies())
                .header("X-CSRF-TOKEN", before.jsonPath().getString("token"))
                .contentType(ContentType.JSON).body(Map.of("identityToken", "test-id-token", "authorizationCode", "test-code"))
                .post("/api/v1/auth/login/apple/native").then().statusCode(200).extract().response();
        assertThat(login.getCookie(sessionCookie)).isNotBlank().isNotEqualTo(before.getCookie(sessionCookie));
        RestAssured.given().cookies(before.getCookies()).get("/api/v1/members/me").then().statusCode(401);
        RestAssured.given().cookies(login.getCookies()).get("/api/v1/members/me").then().statusCode(200);
        RestAssured.given().cookies(login.getCookies()).header("X-CSRF-TOKEN", before.jsonPath().getString("token"))
                .contentType(ContentType.JSON).body(Map.of("nickname", "invalid..nickname"))
                .patch("/api/v1/members/me").then().statusCode(403);
        Response after = RestAssured.given().cookies(login.getCookies()).get("/api/v1/auth/csrf")
                .then().statusCode(200).extract().response();
        RestAssured.given().cookies(login.getCookies()).header("X-CSRF-TOKEN", after.jsonPath().getString("token"))
                .contentType(ContentType.JSON).body(Map.of("nickname", "invalid..nickname"))
                .patch("/api/v1/members/me").then().statusCode(400);
    }


    @Test
    @ResetsAcceptanceData
    void 가입_완료도_세션과_CSRF를_교체하면서_인증_정보를_유지한다() {
        appleService.allowNativeSignup(PendingOAuth2Member.builder()
                .provider("apple").providerId("native-signup-test").build());
        Response before = csrf();
        String sessionCookie = before.getCookies().keySet().iterator().next();
        RestAssured.given().cookies(before.getCookies()).header("X-CSRF-TOKEN", before.jsonPath().getString("token"))
                .contentType(ContentType.JSON).body(Map.of("identityToken", "test-id-token", "authorizationCode", "test-code",
                        "email", "native-signup@example.test", "nickname", "native.signup"))
                .post("/api/v1/auth/login/apple/native").then().statusCode(200);
        Response signedUp = RestAssured.given().cookies(before.getCookies())
                .header("X-CSRF-TOKEN", before.jsonPath().getString("token"))
                .contentType(ContentType.JSON).body(Map.of("email", "native-signup@bombom.news", "nickname", "native.signup"))
                .post("/api/v1/auth/signup").then().statusCode(201).extract().response();
        assertThat(signedUp.getCookie(sessionCookie)).isNotBlank().isNotEqualTo(before.getCookie(sessionCookie));
        RestAssured.given().cookies(before.getCookies()).get("/api/v1/members/me").then().statusCode(401);
        RestAssured.given().cookies(signedUp.getCookies()).get("/api/v1/members/me").then().statusCode(200)
                .body("email", org.hamcrest.Matchers.equalTo("native-signup@bombom.news"));
        RestAssured.given().cookies(signedUp.getCookies()).header("X-CSRF-TOKEN", before.jsonPath().getString("token"))
                .post("/api/v1/auth/logout").then().statusCode(403);
        Response after = RestAssured.given().cookies(signedUp.getCookies()).get("/api/v1/auth/csrf")
                .then().statusCode(200).extract().response();
        RestAssured.given().cookies(signedUp.getCookies()).header("X-CSRF-TOKEN", after.jsonPath().getString("token"))
                .post("/api/v1/auth/logout").then().statusCode(204);
    }

    @Test
    void 토큰_없는_정보_변경은_허용된_Origin에서도_차단한다() {
        RestAssured.given().header(MEMBER_ID, 1L).header("Origin", "http://localhost:3000")
                .contentType(ContentType.JSON).body(Map.of("nickname", "invalid..nickname"))
                .when().patch("/api/v1/members/me")
                .then().statusCode(403);
    }

    @Test
    void 같은_세션의_토큰을_보내면_정상적인_입력_검증까지_진행한다() {
        Response token = csrf();
        RestAssured.given().header(MEMBER_ID, 1L).cookies(token.getCookies())
                .header(token.jsonPath().getString("headerName"), token.jsonPath().getString("token"))
                .contentType(ContentType.JSON).body(Map.of("nickname", "invalid..nickname"))
                .when().patch("/api/v1/members/me")
                .then().statusCode(400);
    }

    @Test
    @ResetsAcceptanceData
    void 유효한_토큰으로_회원_정보를_실제로_변경한다() {
        Response token = csrf();
        RestAssured.given().header(MEMBER_ID, 1L).cookies(token.getCookies())
                .header(token.jsonPath().getString("headerName"), token.jsonPath().getString("token"))
                .contentType(ContentType.JSON).body(Map.of("nickname", "csrf.test"))
                .when().patch("/api/v1/members/me").then().statusCode(200);
        Response member = RestAssured.given().header(MEMBER_ID, 1L).cookies(token.getCookies())
                .when().get("/api/v1/members/me").then().statusCode(200).extract().response();
        assertThat(member.jsonPath().getString("nickname")).isEqualTo("csrf.test");
    }

    @Test
    void 다른_세션의_토큰과_변조된_토큰을_거부한다() {
        Response first = csrf();
        Response second = csrf();
        for (String token : java.util.List.of(first.jsonPath().getString("token"), "invalid-token")) {
            RestAssured.given().header(MEMBER_ID, 1L).cookies(second.getCookies())
                    .header("X-CSRF-TOKEN", token).contentType(ContentType.JSON)
                    .body(Map.of("nickname", "invalid..nickname"))
                    .when().patch("/api/v1/members/me")
                    .then().statusCode(403);
        }
    }

    @Test
    void 로그아웃도_토큰이_필요하고_로그아웃_후_이전_토큰을_거부한다() {
        Response token = csrf();
        RestAssured.given().cookies(token.getCookies()).when().post("/api/v1/auth/logout")
                .then().statusCode(403);
        RestAssured.given().cookies(token.getCookies())
                .header("X-CSRF-TOKEN", token.jsonPath().getString("token"))
                .when().post("/api/v1/auth/logout").then().statusCode(204);
        RestAssured.given().cookies(token.getCookies())
                .header("X-CSRF-TOKEN", token.jsonPath().getString("token"))
                .when().post("/api/v1/auth/logout").then().statusCode(403);
    }

    @Test
    void 외부_Origin은_CSRF_토큰을_읽을_수_없다() {
        var response = RestAssured.given().header("Origin", "https://evil.example")
                .when().get("/api/v1/auth/csrf").then().statusCode(403).extract().response();
        assertThat(response.header("Access-Control-Allow-Origin")).isNull();
    }

    @Test
    void 애플_POST는_CSRF_토큰_없이도_OAuth_검증에서_실패한다() {
        RestAssured.given().redirects().follow(false).header("Origin", "https://appleid.apple.com")
                .contentType(ContentType.URLENC).formParam("code", "invalid-code").formParam("state", "invalid-state")
                .when().post("/login/sso/verify/success/apple")
                .then().statusCode(302).header("Location", "http://localhost:3000/login?error");
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {
            "/login/sso/verify/success/apple/", "/login/sso/verify/success/apple/extra",
            "/login/oauth2/code/apple/extra", "/login/sso/verify/success/google"})
    void 애플_콜백과_비슷한_다른_경로는_CSRF_예외가_아니다(String path) {
        RestAssured.given().redirects().follow(false).contentType(ContentType.URLENC)
                .formParam("code", "unused-code").formParam("state", "unused-state")
                .post(path).then().statusCode(403);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"PUT", "PATCH", "DELETE"})
    void 애플_콜백도_POST_외의_변경_메서드는_CSRF를_검증한다(String method) {
        RestAssured.given().redirects().follow(false).contentType(ContentType.URLENC)
                .formParam("code", "unused-code").formParam("state", "unused-state")
                .request(method, "/login/sso/verify/success/apple").then().statusCode(403);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {
            "/login/sso/verify/success/apple", "/login/oauth2/code/apple"})
    void 애플_콜백은_외부_Origin과_Apple의_GET_요청을_허용하지_않는다(String path) {
        RestAssured.given().header("Origin", "https://evil.example")
                .contentType(ContentType.URLENC).formParam("state", "unused-state")
                .post(path).then().statusCode(403);
        RestAssured.given().header("Origin", "https://appleid.apple.com")
                .get(path).then().statusCode(403);
    }

    private Response csrf() {
        Response response = RestAssured.given().when().get("/api/v1/auth/csrf")
                .then().statusCode(200).extract().response();
        assertThat(response.jsonPath().getBoolean("enabled")).isTrue();
        assertThat(response.header("Cache-Control")).contains("no-store");
        assertThat(response.jsonPath().getString("token")).isNotBlank();
        return response;
    }
}
