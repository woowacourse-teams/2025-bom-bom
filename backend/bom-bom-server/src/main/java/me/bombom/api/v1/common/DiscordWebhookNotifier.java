package me.bombom.api.v1.common;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.member.service.MemberService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordWebhookNotifier {

    @Value("${discord.webhook.new_member.url}")
    private String newMemberWebhookUrl;

    private final WebhookHttpClient webhookClient;
    private final MemberService memberService;

    public void sendNewMemberNotification(String nickname) {
        long totalMemberCount = memberService.countNormalMembers();

        Map<String, Object> body = Map.of("embeds", List.of(
                Map.of(
                        "title", "🎉 새로운 봄봄 회원이 가입했어요!",
                        "color", 0x00C853,
                        "fields", List.of(
                                Map.of("name", "🧑‍💻 닉네임 : ", "value", "**" + nickname + "**", "inline", true),
                                Map.of("name", "🕒 가입 시각 : ", "value", "<t:" + (System.currentTimeMillis() / 1000) + ":F>", "inline", true),
                                Map.of("name", "🌸 현재 총 회원 수 : ", "value", totalMemberCount + "명")
                        )
                )
        ));

        webhookClient.post(newMemberWebhookUrl, body);
    }
}
