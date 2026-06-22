package me.bombom.api.v1.challenge.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import me.bombom.support.time.MutableClock;
import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.AcceptanceTestHeaders;
import me.bombom.support.acceptance.AdditionalAcceptanceDataSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@AcceptanceTest("acceptance/challenge/list-base.json")
class ChallengeControllerTest {

    private static final long MEMBER_ID = 1L;
    private static final LocalDate TODAY = LocalDate.of(2026, 1, 26);

    @Autowired
    private MutableClock clock;

    @BeforeEach
    void 시간을_고정한다() {
        clock.setDate(TODAY);
    }

    @Test
    @AdditionalAcceptanceDataSet("acceptance/challenge/list-not-joined.json")
    void 비로그인_상태로_챌린지_목록_조회() {
        List<Map<String, Object>> result = getChallenges(null, null);
        Map<String, Object> challenge = result.getFirst();

        assertSoftly(softly -> {
            softly.assertThat(challenge.get("id")).isEqualTo(1);
            softly.assertThat(challenge.get("title")).isEqualTo("챌린지");
            softly.assertThat(challenge).containsKeys("participantCount", "newsletters", "status");
            softly.assertThat(participationInfo(challenge).get("isJoined")).isEqualTo(false);
            softly.assertThat(newsletters(challenge)).isNotEmpty();
        });
    }

    @Test
    @AdditionalAcceptanceDataSet("acceptance/challenge/list-joined.json")
    void 로그인_상태로_챌린지_목록_조회() {
        List<Map<String, Object>> result = getChallenges(MEMBER_ID, null);
        Map<String, Object> challenge = result.getFirst();

        assertSoftly(softly -> {
            softly.assertThat(challenge.get("id")).isEqualTo(1);
            softly.assertThat(participationInfo(challenge).get("isJoined")).isEqualTo(true);
            softly.assertThat(participationInfo(challenge)).containsKey("progress");
        });
    }

    @Test
    void 챌린지가_없을_때_빈_배열_반환() {
        List<Map<String, Object>> result = getChallenges(null, null);

        assertThat(result).isEmpty();
    }

    @Test
    @AdditionalAcceptanceDataSet("acceptance/challenge/list-not-joined.json")
    void view_summary_파라미터로_요약_목록_조회() {
        List<Map<String, Object>> result = getChallenges(null, "summary");

        assertThat(result.getFirst().get("id")).isEqualTo(1);
    }

    @Test
    void view_허용되지_않은_값이면_400_반환() {
        RestAssuredMockMvc.given()
                .accept(ContentType.JSON)
                .queryParam("view", "invalid")
                .when()
                .get("/api/v1/challenges")
                .then()
                .statusCode(400);
    }

    private static List<Map<String, Object>> getChallenges(Long memberId, String view) {
        MockMvcRequestSpecification request = RestAssuredMockMvc.given()
                .accept(ContentType.JSON);
        if (memberId != null) {
            request.header(AcceptanceTestHeaders.MEMBER_ID, memberId);
        }
        if (view != null) {
            request.queryParam("view", view);
        }

        return request
                .when()
                .get("/api/v1/challenges")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getList("$");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> participationInfo(Map<String, Object> challenge) {
        return (Map<String, Object>) challenge.get("participationInfo");
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> newsletters(Map<String, Object> challenge) {
        return (List<Map<String, Object>>) challenge.get("newsletters");
    }
}
