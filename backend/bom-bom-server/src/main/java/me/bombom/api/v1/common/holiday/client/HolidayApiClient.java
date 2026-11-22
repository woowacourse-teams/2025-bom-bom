package me.bombom.api.v1.common.holiday.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.common.holiday.domain.Holiday;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class HolidayApiClient {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final int NUM_OF_ROWS = 100;
    private static final String SUCCESS_CODE = "00";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${holiday.api.url}")
    private String url;

    @Value("${holiday.api.service-key}")
    private String serviceKey;

    public HolidayApiClient(RestClient.Builder restClientBuilder, ObjectMapper objectMapper) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(REQUEST_TIMEOUT);
        factory.setReadTimeout(REQUEST_TIMEOUT);
        this.restClient = restClientBuilder
                .requestFactory(factory)
                .build();
        this.objectMapper = objectMapper;
    }

    public List<Holiday> getHolidays(Year year) {
        String response = restClient.get()
                .uri(UriComponentsBuilder.fromUriString(url)
                        .queryParam("serviceKey", "{serviceKey}")
                        .queryParam("solYear", year.getValue())
                        .queryParam("numOfRows", NUM_OF_ROWS)
                        .queryParam("_type", "json")
                        .encode()
                        .buildAndExpand(serviceKey)
                        .toUri())
                .retrieve()
                .body(String.class);

        return parse(response);
    }

    private List<Holiday> parse(String response) {
        JsonNode root = readTree(response).path("response");
        if (!SUCCESS_CODE.equals(root.path("header").path("resultCode").asText())) {
            throw new IllegalStateException("공휴일 API 응답 오류: " + response);
        }

        List<Holiday> holidays = new ArrayList<>();
        for (JsonNode item : getItems(root.path("body").path("items"))) {
            if ("Y".equals(item.path("isHoliday").asText())) {
                holidays.add(Holiday.builder()
                        .date(LocalDate.parse(item.path("locdate").asText(), DateTimeFormatter.BASIC_ISO_DATE))
                        .name(item.path("dateName").asText())
                        .build());
            }
        }
        return holidays;
    }

    /**
     * 결과가 0건이면 items가 빈 문자열, 1건이면 item이 객체, 2건 이상이면 배열로 내려온다.
     */
    private List<JsonNode> getItems(JsonNode items) {
        JsonNode item = items.path("item");
        if (item.isArray()) {
            List<JsonNode> result = new ArrayList<>();
            item.forEach(result::add);
            return result;
        }
        if (item.isObject()) {
            return List.of(item);
        }
        return List.of();
    }

    private JsonNode readTree(String response) {
        try {
            return objectMapper.readTree(response);
        } catch (Exception e) {
            throw new IllegalStateException("공휴일 API 응답을 파싱할 수 없습니다: " + response, e);
        }
    }
}
