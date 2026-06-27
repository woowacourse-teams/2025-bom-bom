package me.bombom.api.v1.challenge.controller;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.restassured.http.ContentType;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.AcceptanceTestHeaders;
import me.bombom.support.time.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@AcceptanceTest("acceptance/challenge/progress.json")
class ChallengeProgressControllerTest {

    private static final long MEMBER_ID = 1L;
    private static final long OTHER_MEMBER_ID = 2L;
    private static final LocalDate TODAY = LocalDate.of(2026, 6, 22);

    @Autowired
    private MutableClock clock;

    @BeforeEach
    void 시간을_고정한다() {
        clock.setDate(TODAY);
    }

    @Test
    void 내_진행상황을_조회한다() {
        Map<String, Object> result = getMemberProgress(1, MEMBER_ID);
        List<Map<String, Object>> todayTodos = todayTodos(result);

        assertSoftly(softly -> {
            softly.assertThat(result.get("nickname")).isEqualTo("userA");
            softly.assertThat(result.get("totalDays")).isEqualTo(10);
            softly.assertThat(result.get("isSurvived")).isEqualTo(false);
            softly.assertThat(result.get("completedDays")).isEqualTo(2);
            softly.assertThat(result.get("streak")).isEqualTo(2);
            softly.assertThat(result.get("shield")).isEqualTo(0);
            softly.assertThat(todayTodos).hasSize(2);
            softly.assertThat(todayTodos)
                    .extracting(todo -> todo.get("challengeTodoType"))
                    .containsExactlyInAnyOrder("READ", "COMMENT");
            softly.assertThat(todayTodos)
                    .filteredOn(todo -> "READ".equals(todo.get("challengeTodoType")))
                    .extracting(todo -> todo.get("challengeTodoStatus"))
                    .containsExactly("COMPLETE");
        });
    }

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

    @Test
    void 챌린지_스트릭을_조회한다() {
        Map<String, Object> result = getMemberStreak(1, MEMBER_ID, null);
        List<Map<String, Object>> streakDays = streakDays(result);

        assertSoftly(softly -> {
            softly.assertThat(result.get("streak")).isEqualTo(2);
            softly.assertThat(streakDays).hasSize(2);
            softly.assertThat(streakDays.get(0).get("date")).isEqualTo("2026-06-21");
            softly.assertThat(streakDays.get(0).get("isCompleted")).isEqualTo(true);
            softly.assertThat(streakDays.get(0).get("isShieldApplied")).isEqualTo(false);
            softly.assertThat(streakDays.get(1).get("date")).isEqualTo("2026-06-22");
            softly.assertThat(streakDays.get(1).get("isShieldApplied")).isEqualTo(true);
        });
    }

    @Test
    void 챌린지_스트릭_limit_파라미터를_적용한다() {
        Map<String, Object> result = getMemberStreak(1, MEMBER_ID, 1);
        List<Map<String, Object>> streakDays = streakDays(result);

        assertSoftly(softly -> {
            softly.assertThat(result.get("streak")).isEqualTo(2);
            softly.assertThat(streakDays).hasSize(1);
            softly.assertThat(streakDays.getFirst().get("date")).isEqualTo("2026-06-22");
        });
    }

    @Test
    void 수료증_정보를_조회한다() {
        clock.setDate(LocalDate.of(2026, 6, 28));

        Map<String, Object> result = getCertificationInfo(1, OTHER_MEMBER_ID);

        assertSoftly(softly -> {
            softly.assertThat(result.get("nickname")).isEqualTo("userB");
            softly.assertThat(result.get("challengeName")).isEqualTo("Test Challenge");
            softly.assertThat(result.get("generation")).isEqualTo(1);
            softly.assertThat(result.get("startDate")).isEqualTo("2026-06-17");
            softly.assertThat(result.get("endDate")).isEqualTo("2026-06-27");
            softly.assertThat(result.get("medal")).isEqualTo("FAIL");
            softly.assertThat(result.get("medalCondition")).isEqualTo(0);
        });
    }

    @Test
    void 내_진행상황_조회에서_id가_양수가_아니면_400을_반환한다() {
        request(MEMBER_ID)
                .when()
                .get("/api/v1/challenges/{challengeId}/progress/me", -1)
                .then()
                .statusCode(400);
    }

    @Test
    void 팀_진행상황_조회에서_챌린지_id가_양수가_아니면_400을_반환한다() {
        request(MEMBER_ID)
                .when()
                .get("/api/v1/challenges/{challengeId}/progress/teams/{teamId}", -1, 1)
                .then()
                .statusCode(400);
    }

    @Test
    void 팀_진행상황_조회에서_팀_id가_양수가_아니면_400을_반환한다() {
        request(MEMBER_ID)
                .when()
                .get("/api/v1/challenges/{challengeId}/progress/teams/{teamId}", 1, -1)
                .then()
                .statusCode(400);
    }

    @Test
    void 챌린지_스트릭_조회에서_id가_양수가_아니면_400을_반환한다() {
        request(MEMBER_ID)
                .when()
                .get("/api/v1/challenges/{challengeId}/progress/me/streak", -1)
                .then()
                .statusCode(400);
    }

    @Test
    void 챌린지_스트릭_조회에서_limit가_양수가_아니면_400을_반환한다() {
        request(MEMBER_ID)
                .queryParam("limit", 0)
                .when()
                .get("/api/v1/challenges/{challengeId}/progress/me/streak", 1)
                .then()
                .statusCode(400);
    }

    private static Map<String, Object> getMemberProgress(long challengeId, long memberId) {
        return request(memberId)
                .when()
                .get("/api/v1/challenges/{challengeId}/progress/me", challengeId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");
    }

    private static Map<String, Object> getTeamProgress(long challengeId, long teamId) {
        return request(MEMBER_ID)
                .when()
                .get("/api/v1/challenges/{challengeId}/progress/teams/{teamId}", challengeId, teamId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");
    }

    private static Map<String, Object> getMemberStreak(long challengeId, long memberId, Integer limit) {
        RequestSpecification request = request(memberId);
        if (limit != null) {
            request.queryParam("limit", limit);
        }

        return request
                .when()
                .get("/api/v1/challenges/{challengeId}/progress/me/streak", challengeId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");
    }

    private static Map<String, Object> getCertificationInfo(long challengeId, long memberId) {
        return request(memberId)
                .when()
                .get("/api/v1/challenges/{challengeId}/certification", challengeId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getMap("$");
    }

    private static RequestSpecification request(long memberId) {
        return RestAssured.given()
                .accept(ContentType.JSON)
                .header(AcceptanceTestHeaders.MEMBER_ID, memberId);
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> todayTodos(Map<String, Object> result) {
        return (List<Map<String, Object>>) result.get("todayTodos");
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> streakDays(Map<String, Object> result) {
        return (List<Map<String, Object>>) result.get("streakDays");
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
