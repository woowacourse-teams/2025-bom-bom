package me.bombom.api.v1.challenge.controller;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.util.List;
import java.util.Map;
import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.AcceptanceTestHeaders;
import org.junit.jupiter.api.Test;

@AcceptanceTest("acceptance/challenge/progress.json")
class ChallengeProgressControllerTest {

    private static final long MEMBER_ID = 1L;

    @Test
    void 특정_팀_진행상황을_조회한다() {
        Map<String, Object> result = getTeamProgress(1, 1);
        List<Map<String, Object>> members = members(result);
        List<Map<String, Object>> firstMemberProgresses = dailyProgresses(members.getFirst());

        assertSoftly(softly -> {
            softly.assertThat(teamSummary(result).get("achievementAverage")).isEqualTo(77);
            softly.assertThat(members).hasSize(2);
            softly.assertThat(members.get(0).get("nickname")).isEqualTo("userB");
            softly.assertThat(firstMemberProgresses).hasSize(2);
            softly.assertThat(firstMemberProgresses.get(0).get("status")).isEqualTo("COMPLETE");
            softly.assertThat(firstMemberProgresses.get(1).get("status")).isEqualTo("SHIELD");
            softly.assertThat(members.get(1).get("nickname")).isEqualTo("userA");
        });
    }

    private static Map<String, Object> getTeamProgress(long challengeId, long teamId) {
        return RestAssuredMockMvc.given()
                .accept(ContentType.JSON)
                .header(AcceptanceTestHeaders.MEMBER_ID, MEMBER_ID)
                .when()
                .get("/api/v1/challenges/{challengeId}/progress/teams/{teamId}", challengeId, teamId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> teamSummary(Map<String, Object> result) {
        return (Map<String, Object>) result.get("teamSummary");
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> members(Map<String, Object> result) {
        return (List<Map<String, Object>>) result.get("members");
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> dailyProgresses(Map<String, Object> member) {
        return (List<Map<String, Object>>) member.get("dailyProgresses");
    }
}
