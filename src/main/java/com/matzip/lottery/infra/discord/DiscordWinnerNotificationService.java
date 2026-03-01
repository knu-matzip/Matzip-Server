package com.matzip.lottery.infra.discord;

import com.matzip.common.config.DiscordWebhookProperties;
import com.matzip.lottery.domain.LotteryEvent;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class DiscordWinnerNotificationService {

    private final WebClient.Builder webClientBuilder;
    private final DiscordWebhookProperties discordWebhookProperties;

    public DiscordWinnerNotificationService(WebClient.Builder webClientBuilder,
                                            DiscordWebhookProperties discordWebhookProperties) {
        this.webClientBuilder = webClientBuilder;
        this.discordWebhookProperties = discordWebhookProperties;
    }


    @Async
    public void notifyWinnerContactSubmitted(LotteryEvent event, Long userId, String phoneNumber) {
        String webhookUrl = discordWebhookProperties.getWebhookUrl();
        if (!StringUtils.hasText(webhookUrl)) {
            log.debug("Discord webhook URL이 설정되지 않아 알림을 건너뜁니다.");
            return;
        }

        String content = buildMessage(event, userId, phoneNumber);
        Map<String, String> body = Map.of("content", content);

        try {
            webClientBuilder.build()
                    .post()
                    .uri(webhookUrl)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("[Discord 알림] 이벤트 당첨자 연락처 제출 알림 전송 완료. eventId: {}, userId: {}", event.getId(), userId);
        } catch (Exception e) {
            log.warn("[Discord 알림] 웹훅 전송 실패. eventId: {}, userId: {}, error: {}", event.getId(), userId, e.getMessage());
        }
    }

    private String buildMessage(LotteryEvent event, Long userId, String phoneNumber) {
        String prizeDesc = event.getPrize() != null ? event.getPrize().getDescription() : "-";
        return """
                **이벤트 당첨자 연락처 제출**
                - 이벤트 ID: %d
                - 상품: %s
                - 유저 ID: %d
                - 연락처: %s
                """
                .formatted(event.getId(), prizeDesc, userId, phoneNumber);
    }
}
