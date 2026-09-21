package me.bombom.api.v1.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import me.bombom.api.v1.auth.enums.SignupValidateField;
import me.bombom.api.v1.auth.enums.SignupValidateStatus;
import me.bombom.support.acceptance.AcceptanceTest;
import org.junit.jupiter.api.Test;

@AcceptanceTest("acceptance/common/member.json")
class AuthControllerTest {

    private static final String EXISTING_NICKNAME = "인수테스트회원";
    private static final String EXISTING_EMAIL = "acceptance@bombom.news";

    @Test
    void 닉네임_중복_체크_DUPLICATE() {
        String result = checkSignup(SignupValidateField.NICKNAME.name(), EXISTING_NICKNAME);

        assertThat(result).isEqualTo(SignupValidateStatus.DUPLICATE.name());
    }

    @Test
    void 닉네임_형식_체크_INVALID_FORMAT() {
        String result = checkSignup(SignupValidateField.NICKNAME.name(), "bombom..bombom");

        assertThat(result).isEqualTo(SignupValidateStatus.INVALID_FORMAT.name());
    }

    @Test
    void 회원가입_닉네임_체크_OK() {
        String result = checkSignup(SignupValidateField.NICKNAME.name(), "anotherNickname");

        assertThat(result).isEqualTo(SignupValidateStatus.OK.name());
    }

    @Test
    void 이메일_중복_체크_DUPLICATE() {
        String result = checkSignup(SignupValidateField.EMAIL.name(), EXISTING_EMAIL);

        assertThat(result).isEqualTo(SignupValidateStatus.DUPLICATE.name());
    }

    @Test
    void 유효하지_않은_필드_입력_시_예외() {
        RestAssured.given()
                .accept(ContentType.JSON)
                .queryParam("field", "INVALID")
                .queryParam("userInput", EXISTING_EMAIL)
                .when()
                .get("/api/v1/auth/signup/check")
                .then()
                .statusCode(400);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"google", "apple"})
    void 세션_없는_OAuth_콜백은_기존_실패_페이지로_리다이렉트한다(String provider) {
        var response = RestAssured.given().redirects().follow(false)
                .queryParam("code", "unused-test-code").queryParam("state", "unused-test-state")
                .when().get("/login/oauth2/code/" + provider)
                .then().statusCode(302).extract().response();
        assertThat(response.header("Location")).endsWith("/login?error");
    }

    private static String checkSignup(String field, String userInput) {
        return RestAssured.given()
                .accept(ContentType.JSON)
                .queryParam("field", field)
                .queryParam("userInput", userInput)
                .when()
                .get("/api/v1/auth/signup/check")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .asString()
                .replace("\"", "");
    }
}
