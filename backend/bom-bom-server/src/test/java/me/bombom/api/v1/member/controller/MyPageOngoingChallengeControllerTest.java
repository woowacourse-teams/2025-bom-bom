package me.bombom.api.v1.member.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import java.time.LocalDate;
import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.AcceptanceTestHeaders;
import me.bombom.support.time.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@AcceptanceTest("acceptance/mypage/ongoing-challenges.json")
class MyPageOngoingChallengeControllerTest {

    private static final long MEMBER_ID = 1L;
    private static final long MEMBER_WITHOUT_ONGOING = 6L;
    private static final LocalDate TODAY = LocalDate.of(2026, 6, 16);

    @Autowired
    private MutableClock clock;

    @BeforeEach
    void 시간을_고정한다() {
        clock.setDate(TODAY);
    }

    @Test
    void 참여중_챌린지_목록을_팀_정보와_함께_조회한다() {
        // member1: 진행중 챌린지 101(end 06-30), 102(end 07-10) 참여. 종료(200)/미참여(103)는 제외, endDate 오름차순 정렬
        JsonPath body = getOngoing(MEMBER_ID);

        assertSoftly(softly -> {
            softly.assertThat(body.getList("challenges")).hasSize(2);
            softly.assertThat(body.getInt("challenges[0].challengeId")).isEqualTo(101);
            softly.assertThat(body.getInt("challenges[1].challengeId")).isEqualTo(102);

            // 챌린지 101 — 팀1001[나 80%, 100%], 팀1002[60%, 40%]
            softly.assertThat(body.getString("challenges[0].title")).isEqualTo("진행중 챌린지A");
            softly.assertThat(body.getInt("challenges[0].remainingDays")).isEqualTo(14);
            softly.assertThat(body.getInt("challenges[0].progressRate")).isEqualTo(80);
            softly.assertThat(body.getInt("challenges[0].myTeamRank.rank")).isEqualTo(2);
            softly.assertThat(body.getInt("challenges[0].myTeamRank.totalMembers")).isEqualTo(2);
            softly.assertThat(body.getInt("challenges[0].teamRank.rank")).isEqualTo(1);
            softly.assertThat(body.getInt("challenges[0].teamRank.totalTeams")).isEqualTo(2);
            softly.assertThat(body.getInt("challenges[0].myAttendanceComparison.attendanceRate")).isEqualTo(80);
            softly.assertThat(body.getInt("challenges[0].myAttendanceComparison.differencePoint")).isEqualTo(10);
            softly.assertThat(body.getInt("challenges[0].teamAttendanceComparison.teamAttendanceRate")).isEqualTo(90);
            softly.assertThat(body.getInt("challenges[0].teamAttendanceComparison.differencePoint")).isEqualTo(20);
        });
    }

    @Test
    void 참여중_챌린지가_없으면_빈_배열을_반환한다() {
        // member6: 종료된 챌린지에만 참여
        JsonPath body = getOngoing(MEMBER_WITHOUT_ONGOING);

        assertThat(body.getList("challenges")).isEmpty();
    }

    @Test
    void 비인증_상태로_조회하면_401_을_반환한다() {
        RestAssured.given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/members/me/challenges/ongoing")
                .then()
                .statusCode(401);
    }

    private static JsonPath getOngoing(long memberId) {
        return request(memberId)
                .when()
                .get("/api/v1/members/me/challenges/ongoing")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath();
    }

    private static RequestSpecification request(long memberId) {
        return RestAssured.given()
                .accept(ContentType.JSON)
                .header(AcceptanceTestHeaders.MEMBER_ID, memberId);
    }
}
