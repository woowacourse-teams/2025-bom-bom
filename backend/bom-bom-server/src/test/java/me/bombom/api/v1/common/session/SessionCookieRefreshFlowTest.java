package me.bombom.api.v1.common.session;

import static me.bombom.support.acceptance.AcceptanceTestHeaders.MEMBER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import me.bombom.support.acceptance.AcceptanceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;

@AcceptanceTest({
        "acceptance/common/member.json",
        "acceptance/member/member.json"
})
class SessionCookieRefreshFlowTest {

    private static final long MEMBER_ID_VALUE = 1L;
    private static final int CONCURRENT_REQUEST_COUNT = 8;

    @Autowired
    private SessionRepository<? extends Session> sessionRepository;

    @Value("${server.servlet.session.cookie.name:JSESSIONID}")
    private String sessionCookieName;

    @Test
    void 로그인된_회원의_요청마다_같은_세션_ID로_세션_쿠키를_다시_내려보낸다() {
        // given
        String encodedSessionId = encode(createSession(sessionRepository));

        // when
        Response firstResponse = requestMyProfile(encodedSessionId);
        Response secondResponse = requestMyProfile(encodedSessionId);

        // then
        assertSoftly(softly -> {
            softly.assertThat(firstResponse.statusCode()).isEqualTo(200);
            softly.assertThat(secondResponse.statusCode()).isEqualTo(200);
            softly.assertThat(firstResponse.header(HttpHeaders.SET_COOKIE))
                    .startsWith(sessionCookieName + "=" + encodedSessionId + ";");
            softly.assertThat(secondResponse.header(HttpHeaders.SET_COOKIE))
                    .startsWith(sessionCookieName + "=" + encodedSessionId + ";");
        });
    }

    @Test
    void 같은_세션으로_동시에_들어온_요청이_모두_성공한다() {
        // given
        String encodedSessionId = encode(createSession(sessionRepository));

        // when
        List<Integer> statusCodes = IntStream.range(0, CONCURRENT_REQUEST_COUNT)
                .mapToObj(index -> CompletableFuture.supplyAsync(() -> requestMyProfile(encodedSessionId).statusCode()))
                .toList()
                .stream()
                .map(CompletableFuture::join)
                .toList();

        // then
        assertThat(statusCodes).containsOnly(200);
    }

    @Test
    void 비로그인_요청에는_세션_ID를_다시_내려보내지_않는다() {
        // given
        String encodedSessionId = encode(createSession(sessionRepository));

        // when
        Response response = RestAssured.given()
                .cookie(sessionCookieName, encodedSessionId)
                .when().get("/api/v1/newsletters")
                .then().statusCode(200)
                .extract().response();

        // then
        // 인증 정보가 없는 세션 쿠키는 기존 동작대로 만료(Max-Age=0)될 수 있으므로, 세션 ID 재발급 여부만 본다.
        assertThat(response.headers().getValues(HttpHeaders.SET_COOKIE))
                .noneMatch(setCookie -> setCookie.startsWith(sessionCookieName + "=" + encodedSessionId));
    }

    private Response requestMyProfile(String encodedSessionId) {
        return RestAssured.given()
                .header(MEMBER_ID, MEMBER_ID_VALUE)
                .cookie(sessionCookieName, encodedSessionId)
                .when().get("/api/v1/members/me/profile")
                .then()
                .extract().response();
    }

    private static <S extends Session> String createSession(SessionRepository<S> repository) {
        S session = repository.createSession();
        repository.save(session);
        return session.getId();
    }

    private static String encode(String sessionId) {
        return Base64.getEncoder().encodeToString(sessionId.getBytes(StandardCharsets.UTF_8));
    }
}
