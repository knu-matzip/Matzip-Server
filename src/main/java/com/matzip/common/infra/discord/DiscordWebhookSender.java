package com.matzip.common.infra.discord;

import com.matzip.common.config.DiscordWebhookProperties;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class DiscordWebhookSender {

    private final WebClient.Builder webClientBuilder;
    private final DiscordWebhookProperties discordWebhookProperties;

    public DiscordWebhookSender(WebClient.Builder webClientBuilder,
                                DiscordWebhookProperties discordWebhookProperties) {
        this.webClientBuilder = webClientBuilder;
        this.discordWebhookProperties = discordWebhookProperties;
    }

    @Async
    public void sendAsync(String content) {
        String webhookUrl = discordWebhookProperties.getWebhookUrl();
        if (!StringUtils.hasText(webhookUrl)) {
            log.debug("Discord webhook URL이 설정되지 않아 알림을 건너뜁니다.");
            return;
        }

        Map<String, String> body = Map.of("content", content);

        try {
            webClientBuilder.build()
                    .post()
                    .uri(webhookUrl)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("[Discord 알림] 전송 완료");
        } catch (Exception e) {
            log.warn("[Discord 알림] 웹훅 전송 실패. error: {}", e.getMessage());
        }
    }
}
