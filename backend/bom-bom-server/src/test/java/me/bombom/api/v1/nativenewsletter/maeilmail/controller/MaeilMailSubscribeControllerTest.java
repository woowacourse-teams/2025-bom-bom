package me.bombom.api.v1.nativenewsletter.maeilmail.controller;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.http.ContentType;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.List;
import java.util.Map;
import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.AcceptanceTestHeaders;
import me.bombom.support.acceptance.ResetsAcceptanceData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@AcceptanceTest({
        "acceptance/common/member.json",
        "acceptance/maeilmail/maeil-mail-newsletter.json"
})
class MaeilMailSubscribeControllerTest {

    private static final long MEMBER_ID = 1L;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void 미구독이면_빈_트랙을_반환한다() {
        Map<String, Object> result = getSubscription();

        assertThat(result.get("tracks")).isEqualTo(List.of());
    }

    @Test
    @ResetsAcceptanceData
    void 트랙을_보내면_매일메일을_신규_구독한다() {
        changeSubscription(List.of("BE", "FE"))
                .then()
                .statusCode(200);

        assertThat(countSubscriptions()).isEqualTo(1);
        assertThat(findTrackFields()).containsExactlyInAnyOrder("BE", "FE");
    }

    @Test
    @ResetsAcceptanceData
    void 구독_중인_트랙을_요청한_트랙으로_치환한다() {
        changeSubscription(List.of("BE", "FE")).then().statusCode(200);
        changeSubscription(List.of("BE")).then().statusCode(200);

        Map<String, Object> result = getSubscription();

        assertThat(result.get("tracks")).isEqualTo(List.of("BE"));
    }

    @Test
    @ResetsAcceptanceData
    void 구독을_삭제하면_구독과_트랙을_모두_삭제한다() {
        changeSubscription(List.of("BE", "FE")).then().statusCode(200);

        deleteSubscription().then().statusCode(200);

        assertThat(countSubscriptions()).isZero();
        assertThat(countTracks()).isZero();
    }

    @Test
    void 미구독_상태의_삭제는_성공한다() {
        deleteSubscription().then().statusCode(200);
    }

    @Test
    void 빈_트랙으로_구독을_요청하면_400을_반환한다() {
        changeSubscription(List.of())
                .then()
                .statusCode(400);

        assertThat(countSubscriptions()).isZero();
    }

    @Test
    void 중복된_트랙으로_구독을_요청하면_400을_반환한다() {
        changeSubscription(List.of("BE", "BE"))
                .then()
                .statusCode(400);

        assertThat(countSubscriptions()).isZero();
    }

    private static Map<String, Object> getSubscription() {
        return authenticatedRequest()
                .when()
                .get("/api/v1/subscriptions/native/maeil-mail")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");
    }

    private static Response changeSubscription(List<String> tracks) {
        return authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(Map.of("tracks", tracks))
                .when()
                .put("/api/v1/subscriptions/native/maeil-mail");
    }

    private static Response deleteSubscription() {
        return authenticatedRequest()
                .when()
                .delete("/api/v1/subscriptions/native/maeil-mail");
    }

    private static RequestSpecification authenticatedRequest() {
        return RestAssured.given()
                .accept(ContentType.JSON)
                .header(AcceptanceTestHeaders.MEMBER_ID, MEMBER_ID);
    }

    private int countSubscriptions() {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from subscribe where member_id = ? and newsletter_id = ?",
                Integer.class,
                MEMBER_ID,
                1L
        );
        return count == null ? 0 : count;
    }

    private int countTracks() {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from maeil_mail_subscription_track where member_id = ?",
                Integer.class,
                MEMBER_ID
        );
        return count == null ? 0 : count;
    }

    private List<String> findTrackFields() {
        return jdbcTemplate.queryForList(
                "select field from maeil_mail_subscription_track where member_id = ? order by field",
                String.class,
                MEMBER_ID
        );
    }
}
