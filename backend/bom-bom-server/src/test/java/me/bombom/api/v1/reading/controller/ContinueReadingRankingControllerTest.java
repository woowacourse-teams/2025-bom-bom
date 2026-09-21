package me.bombom.api.v1.reading.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.AcceptanceDataSetLoader;
import me.bombom.support.acceptance.AdditionalAcceptanceDataSet;
import me.bombom.support.acceptance.ResetsAcceptanceData;
import me.bombom.support.time.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@AcceptanceTest({
        "acceptance/common/member.json",
        "acceptance/reading/reading.json",
        "acceptance/reading/streak-ranking.json"
})
class ContinueReadingRankingControllerTest {

    @Autowired
    private MutableClock clock;

    @Autowired
    private AcceptanceDataSetLoader dataSetLoader;

    @BeforeEach
    void 날짜를_고정한다() {
        clock.setDate(LocalDate.of(2026, 9, 6));
    }

    @Test
    void 공동_순위는_닉네임_순으로_limit만큼_조회하고_선정된_회원의_배지를_반환한다() {
        List<Map<String, Object>> ranking = getRanking(2);

        assertThat(ranking).extracting(row -> row.get("nickname"))
                .containsExactly("Alpha", "Zeta");
        Map<String, Object> first = ranking.getFirst();
        Map<String, Object> badges = nested(first, "badges");
        assertSoftly(softly -> {
            softly.assertThat(first.get("rank")).isEqualTo(1);
            softly.assertThat(first.get("dayCount")).isEqualTo(10);
            softly.assertThat(nested(badges, "monthlyRanking"))
                    .containsEntry("grade", "GOLD")
                    .containsEntry("year", 2026)
                    .containsEntry("month", 8);
            softly.assertThat(nested(badges, "challenge"))
                    .containsEntry("grade", "SILVER")
                    .containsEntry("name", "최근 챌린지")
                    .containsEntry("generation", 2);
            softly.assertThat(nested(badges, "streak")).containsEntry("tier", "THIRTY");
            softly.assertThat(ranking.get(1).get("badges")).isNull();
        });
    }

    @Test
    void limit이_전체_인원보다_크면_모든_회원을_순위와_닉네임_순으로_반환한다() {
        List<Map<String, Object>> ranking = getRanking(10);

        assertSoftly(softly -> {
            softly.assertThat(ranking).extracting(row -> row.get("nickname"))
                    .containsExactly("Alpha", "Zeta", "인수테스트회원", "Lower");
            softly.assertThat(ranking).extracting(row -> row.get("rank"))
                    .containsExactly(1, 1, 1, 4);
        });
    }

    @Test
    @AdditionalAcceptanceDataSet("acceptance/reading/streak-ranking-duplicate-badge.json")
    void 같은_회원의_월간_배지가_중복되어도_응답은_limit을_넘지_않는다() {
        assertThat(getRanking(2)).hasSize(2);
    }

    @Test
    @ResetsAcceptanceData
    void 랭킹_대상이_없으면_빈_목록을_반환한다() {
        dataSetLoader.load("acceptance/reading/streak-ranking-empty.json");

        assertThat(getRanking(2)).isEmpty();
    }

    @Test
    void limit이_0이면_잘못된_요청으로_응답한다() {
        RestAssured.given()
                .queryParam("limit", 0)
                .when()
                .get("/api/v1/members/me/reading/streak/rank")
                .then()
                .statusCode(400);
    }

    private static List<Map<String, Object>> getRanking(int limit) {
        return RestAssured.given()
                .accept(ContentType.JSON)
                .queryParam("limit", limit)
                .when()
                .get("/api/v1/members/me/reading/streak/rank")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getList("data");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> nested(Map<String, Object> source, String key) {
        return (Map<String, Object>) source.get(key);
    }
}
