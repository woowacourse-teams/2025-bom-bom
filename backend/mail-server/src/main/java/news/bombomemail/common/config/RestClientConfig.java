package news.bombomemail.common.config;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    @Bean
    public RestClient discordRestClient(RestClient.Builder builder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(REQUEST_TIMEOUT);
        factory.setReadTimeout(REQUEST_TIMEOUT);
        return builder.requestFactory(factory).build();
    }
}
