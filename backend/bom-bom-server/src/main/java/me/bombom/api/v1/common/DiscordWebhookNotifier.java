package me.bombom.api.v1.common;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordWebhookNotifier {

    @Value("${discord.webhook.new_member.url}")
    private String webhookUrl;

    private final WebhookHttpClient webhookClient;

    public void sendNewMemberNotification(String nickname) {
        Map<String, Object> body = Map.of("embeds", List.of(
                Map.of(
                        "title", "🎉 새로운 봄봄 회원이 가입했어요!",
                        "color", 0x00C853,
                        "fields", List.of(
                                Map.of("name", "🧑‍💻 닉네임", "value", "**" + nickname + "**"),
                                Map.of("name", "🕒 가입 시각", "value", LocalDateTime.now().toString())
                        )
                )
        ));

        webhookClient.post(webhookUrl, body);
    }
}
