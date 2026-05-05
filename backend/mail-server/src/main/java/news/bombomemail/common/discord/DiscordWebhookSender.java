package news.bombomemail.common.discord;

import java.util.List;
import java.util.Map;
import news.bombomemail.subscribe.alert.UnsubscribeUrlFailure;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class DiscordWebhookSender {

    private final RestClient restClient;

    @Value("${discord.webhook.unsubscribe-alert.url}")
    private String webhookUrl;

    public DiscordWebhookSender(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public void sendUnsubscribeUrlMissingAlert(List<UnsubscribeUrlFailure> failures) {
        restClient.post()
                .uri(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("content", formatMessage(failures)))
                .retrieve()
                .toBodilessEntity();
    }

    private String formatMessage(List<UnsubscribeUrlFailure> failures) {
        StringBuilder sb = new StringBuilder();
        sb.append("⚠️ **unsubscribeUrl 파싱 실패 (%d건)**\n".formatted(failures.size()));
        failures.forEach(f ->
                sb.append("• %s | %s | memberId: %d\n".formatted(f.newsletterName(), f.articleTitle(), f.memberId()))
        );
        return sb.toString();
    }
}
