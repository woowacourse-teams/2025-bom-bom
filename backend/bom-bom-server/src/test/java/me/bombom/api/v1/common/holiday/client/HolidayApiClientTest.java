package me.bombom.api.v1.common.holiday.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import me.bombom.api.v1.common.holiday.domain.Holiday;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

class HolidayApiClientTest {

    private HttpServer server;
    private HolidayApiClient holidayApiClient;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.start();
        holidayApiClient = new HolidayApiClient(RestClient.builder(), new ObjectMapper());
        ReflectionTestUtils.setField(holidayApiClient, "url",
                "http://localhost:" + server.getAddress().getPort() + "/getRestDeInfo");
        ReflectionTestUtils.setField(holidayApiClient, "serviceKey", "service-key");
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void 여러_건_응답에서_쉬는_날만_조회한다() {
        // given
        respond("""
                {"response":{"header":{"resultCode":"00","resultMsg":"NORMAL SERVICE."},
                "body":{"items":{"item":[
                  {"dateKind":"01","dateName":"삼일절","isHoliday":"Y","locdate":20260301,"seq":1},
                  {"dateKind":"01","dateName":"대체공휴일(삼일절)","isHoliday":"Y","locdate":20260302,"seq":1},
                  {"dateKind":"01","dateName":"식목일","isHoliday":"N","locdate":20260405,"seq":1}
                ]},"numOfRows":100,"pageNo":1,"totalCount":3}}}
                """);

        // when
        List<Holiday> holidays = holidayApiClient.getHolidays(Year.of(2026));

        // then
        assertThat(holidays)
                .extracting(Holiday::getDate, Holiday::getName)
                .containsExactly(
                        tuple(LocalDate.of(2026, 3, 1), "삼일절"),
                        tuple(LocalDate.of(2026, 3, 2), "대체공휴일(삼일절)")
                );
    }

    @Test
    void 한_건이면_item이_객체로_와도_조회한다() {
        // given
        respond("""
                {"response":{"header":{"resultCode":"00","resultMsg":"NORMAL SERVICE."},
                "body":{"items":{"item":
                  {"dateKind":"01","dateName":"제헌절","isHoliday":"Y","locdate":20260717,"seq":1}
                },"numOfRows":100,"pageNo":1,"totalCount":1}}}
                """);

        // when
        List<Holiday> holidays = holidayApiClient.getHolidays(Year.of(2026));

        // then
        assertThat(holidays)
                .extracting(Holiday::getDate, Holiday::getName)
                .containsExactly(tuple(LocalDate.of(2026, 7, 17), "제헌절"));
    }

    @Test
    void 결과가_없으면_빈_목록을_반환한다() {
        // given
        respond("""
                {"response":{"header":{"resultCode":"00","resultMsg":"NORMAL SERVICE."},
                "body":{"items":"","numOfRows":100,"pageNo":1,"totalCount":0}}}
                """);

        // when
        List<Holiday> holidays = holidayApiClient.getHolidays(Year.of(2027));

        // then
        assertThat(holidays).isEmpty();
    }

    @Test
    void resultCode가_00이_아니면_예외가_발생한다() {
        // given
        respond("""
                {"response":{"header":{"resultCode":"30","resultMsg":"SERVICE_KEY_IS_NOT_REGISTERED_ERROR"}}}
                """);

        // when & then
        assertThatThrownBy(() -> holidayApiClient.getHolidays(Year.of(2026)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SERVICE_KEY_IS_NOT_REGISTERED_ERROR");
    }

    private void respond(String body) {
        server.createContext("/getRestDeInfo", exchange -> sendResponse(exchange, body));
    }

    private void sendResponse(HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
