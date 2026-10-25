package me.bombom.api.v1.auth.controller;

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
@TestPropertySource(properties = "app.security.csrf-enabled=false")
class CsrfDisabledControllerTest {

    @Autowired
    private FakeAppleOAuth2Service appleOAuth2Service;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 비활성_상태에서는_CSRF_토큰을_발급하지_않는다() {
        Response response = RestAssured.given().get("/api/v1/auth/csrf")
                .then().statusCode(200).extract().response();

        assertThat(response.jsonPath().getBoolean("enabled")).isFalse();
        assertThat(response.jsonPath().getString("token")).isNull();
        assertThat(response.jsonPath().getString("headerName")).isNull();
        assertThat(response.header("Cache-Control")).contains("no-store");
    }

    @Test
    @ResetsAcceptanceData
    void 기존_클라이언트는_토큰_없이_로그인하고_정보를_변경하고_로그아웃한다() {
        appleOAuth2Service.allowNativeLogin(memberRepository.findById(1L).orElseThrow());
        Response login = nativeLogin();

        RestAssured.given().cookies(login.getCookies()).header("Origin", "http://localhost:3000")
                .contentType(ContentType.JSON).body(Map.of("nickname", "legacy.client"))
                .patch("/api/v1/members/me").then().statusCode(200);
        RestAssured.given().cookies(login.getCookies()).get("/api/v1/members/me")
                .then().statusCode(200).body("nickname", org.hamcrest.Matchers.equalTo("legacy.client"));
        RestAssured.given().cookies(login.getCookies()).post("/api/v1/auth/logout")
                .then().statusCode(204);
        RestAssured.given().cookies(login.getCookies()).get("/api/v1/members/me")
                .then().statusCode(401);
    }

    @Test
    @ResetsAcceptanceData
    void 기존_클라이언트는_CSRF_토큰_없이_가입을_완료한다() {
        appleOAuth2Service.allowNativeSignup(PendingOAuth2Member.builder()
                .provider("apple").providerId("legacy-signup-test").build());
        Response pending = nativeLogin();

        Response signup = RestAssured.given().cookies(pending.getCookies())
                .contentType(ContentType.JSON)
                .body(Map.of("email", "legacy-signup@bombom.news", "nickname", "legacy.signup"))
                .post("/api/v1/auth/signup").then().statusCode(201).extract().response();
        RestAssured.given().cookies(signup.getCookies()).get("/api/v1/members/me")
                .then().statusCode(200).body("email", org.hamcrest.Matchers.equalTo("legacy-signup@bombom.news"));
    }

    @Test
    void 토큰_검사를_꺼도_외부_Origin의_인증된_변경_요청은_CORS가_거부한다() {
        appleOAuth2Service.allowNativeLogin(memberRepository.findById(1L).orElseThrow());
        Response login = nativeLogin();

        Response response = RestAssured.given().cookies(login.getCookies())
                .header("Origin", "https://evil.example")
                .contentType(ContentType.JSON).body(Map.of("nickname", "attack.name"))
                .patch("/api/v1/members/me").then().statusCode(403).extract().response();
        assertThat(response.header("Access-Control-Allow-Origin")).isNull();
        assertThat(memberRepository.findById(1L).orElseThrow().getNickname()).isEqualTo("인수테스트회원");
    }

    private Response nativeLogin() {
        return RestAssured.given().contentType(ContentType.JSON)
                .body(Map.of("identityToken", "test-id-token", "authorizationCode", "test-code"))
                .post("/api/v1/auth/login/apple/native").then().statusCode(200).extract().response();
    }
}
