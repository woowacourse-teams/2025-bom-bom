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

@AcceptanceTest("acceptance/mypage/completed-challenges.json")
class MyPageCompletedChallengeControllerTest {

    private static final long MEMBER_ID = 1L;
    private static final long MEMBER_WITHOUT_COMPLETED = 6L;
    private static final LocalDate TODAY = LocalDate.of(2026, 6, 16);

    @Autowired
    private MutableClock clock;

    @BeforeEach
    void 시간을_고정한다() {
        clock.setDate(TODAY);
    }

    @Test
    void 완료한_챌린지를_종료일_내림차순으로_조회하고_등급_FAIL도_포함한다() {
        // member1 종료 챌린지: 301(GOLD,2024-05-09), 302(SILVER,2025-03-20), 303(FAIL,2026-01-31)
        // endDate 내림차순 → [303, 302, 301], 진행중(304)은 제외
        JsonPath body = getCompleted(MEMBER_ID, null, null);

        assertSoftly(softly -> {
            softly.assertThat(body.getInt("totalElements")).isEqualTo(3);
            softly.assertThat(body.getInt("totalPages")).isEqualTo(1);
            softly.assertThat(body.getBoolean("first")).isTrue();
            softly.assertThat(body.getBoolean("last")).isTrue();
            softly.assertThat(body.getList("content")).hasSize(3);

            softly.assertThat(body.getInt("content[0].challengeId")).isEqualTo(303);
            softly.assertThat(body.getString("content[0].grade")).isEqualTo("FAIL");
            softly.assertThat(body.getInt("content[0].attendanceRate")).isEqualTo(70);

            softly.assertThat(body.getInt("content[1].challengeId")).isEqualTo(302);
            softly.assertThat(body.getString("content[1].grade")).isEqualTo("SILVER");
            softly.assertThat(body.getInt("content[1].attendanceRate")).isEqualTo(90);

            softly.assertThat(body.getInt("content[2].challengeId")).isEqualTo(301);
            softly.assertThat(body.getString("content[2].grade")).isEqualTo("GOLD");
            softly.assertThat(body.getInt("content[2].attendanceRate")).isEqualTo(100);
            softly.assertThat(body.getString("content[2].title")).isEqualTo("30일 독서 챌린지");
        });
    }

    @Test
    void 페이징_파라미터가_적용된다() {
        // size=2 → 첫 페이지 2건(303, 302), 총 3건/2페이지
        JsonPath body = getCompleted(MEMBER_ID, 0, 2);

        assertSoftly(softly -> {
            softly.assertThat(body.getList("content")).hasSize(2);
            softly.assertThat(body.getInt("totalElements")).isEqualTo(3);
            softly.assertThat(body.getInt("totalPages")).isEqualTo(2);
            softly.assertThat(body.getBoolean("first")).isTrue();
            softly.assertThat(body.getBoolean("last")).isFalse();
            softly.assertThat(body.getInt("content[0].challengeId")).isEqualTo(303);
            softly.assertThat(body.getInt("content[1].challengeId")).isEqualTo(302);
        });
    }

    @Test
    void 완료한_챌린지가_없으면_빈_목록을_반환한다() {
        // member6: 진행중 챌린지에만 참여
        JsonPath body = getCompleted(MEMBER_WITHOUT_COMPLETED, null, null);

        assertSoftly(softly -> {
            softly.assertThat(body.getInt("totalElements")).isZero();
            softly.assertThat(body.getList("content")).isEmpty();
        });
    }

    @Test
    void 비인증_상태로_조회하면_401_을_반환한다() {
        RestAssured.given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/members/me/challenges/completed")
                .then()
                .statusCode(401);
    }

    private static JsonPath getCompleted(long memberId, Integer page, Integer size) {
        RequestSpecification request = request(memberId);
        if (page != null) {
            request.queryParam("page", page);
        }
        if (size != null) {
            request.queryParam("size", size);
        }
        return request
                .when()
                .get("/api/v1/members/me/challenges/completed")
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
