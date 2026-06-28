package me.bombom.api.v1.member.controller;

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

@AcceptanceTest("acceptance/mypage/challenge-summary.json")
class MyPageChallengeControllerTest {

    private static final long MEMBER_ID = 1L;
    private static final long MEMBER_WITHOUT_ENDED_CHALLENGE = 4L;
    private static final LocalDate TODAY = LocalDate.of(2026, 1, 26);

    @Autowired
    private MutableClock clock;

    @BeforeEach
    void 시간을_고정한다() {
        clock.setDate(TODAY);
    }

    @Test
    void 로그인한_회원의_챌린지_요약을_조회한다() {
        // member1: 종료챌린지 2건(10/10 생존 GOLD, 8/10 생존 BRONZE) → 완료 2, 완료율 100, 출석평균 90, 메달 50/0/50
        // 완료율 모집단 [100, 50, 100] → 상위 0.0% / 출석 모집단 [90, 80, 95] → 90 초과 1명 → 33.3%
        JsonPath summary = getSummary(MEMBER_ID);

        assertSoftly(softly -> {
            softly.assertThat(summary.getInt("completedChallengeCount")).isEqualTo(2);
            softly.assertThat(summary.getDouble("completionRank.topPercent")).isEqualTo(0.0);
            softly.assertThat(summary.getInt("completionRank.completionRate")).isEqualTo(100);
            softly.assertThat(summary.getDouble("attendanceRank.topPercent")).isEqualTo(33.3);
            softly.assertThat(summary.getInt("attendanceRank.averageAttendanceRate")).isEqualTo(90);
            softly.assertThat(summary.getInt("medalRatio.gold")).isEqualTo(50);
            softly.assertThat(summary.getInt("medalRatio.silver")).isEqualTo(0);
            softly.assertThat(summary.getInt("medalRatio.bronze")).isEqualTo(50);
        });
    }

    @Test
    void 참여한_종료_챌린지가_없으면_모든_값이_0이다() {
        // member4: 진행중 챌린지에만 참여 → 종료 챌린지 집계 0건
        JsonPath summary = getSummary(MEMBER_WITHOUT_ENDED_CHALLENGE);

        assertSoftly(softly -> {
            softly.assertThat(summary.getInt("completedChallengeCount")).isZero();
            softly.assertThat(summary.getDouble("completionRank.topPercent")).isEqualTo(0.0);
            softly.assertThat(summary.getInt("completionRank.completionRate")).isZero();
            softly.assertThat(summary.getDouble("attendanceRank.topPercent")).isEqualTo(0.0);
            softly.assertThat(summary.getInt("attendanceRank.averageAttendanceRate")).isZero();
            softly.assertThat(summary.getInt("medalRatio.gold")).isZero();
            softly.assertThat(summary.getInt("medalRatio.silver")).isZero();
            softly.assertThat(summary.getInt("medalRatio.bronze")).isZero();
        });
    }

    @Test
    void 비인증_상태로_조회하면_401_을_반환한다() {
        RestAssured.given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/members/me/challenges/summary")
                .then()
                .statusCode(401);
    }

    private static JsonPath getSummary(long memberId) {
        return request(memberId)
                .when()
                .get("/api/v1/members/me/challenges/summary")
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
