package com.matzip.lottery.infra.discord;

import com.matzip.common.infra.discord.DiscordWebhookSender;
import com.matzip.lottery.domain.LotteryEvent;
import org.springframework.stereotype.Service;

@Service
public class DiscordWinnerNotificationService {

    private final DiscordWebhookSender discordWebhookSender;

    public DiscordWinnerNotificationService(DiscordWebhookSender discordWebhookSender) {
        this.discordWebhookSender = discordWebhookSender;
    }

    public void notifyWinnerContactSubmitted(LotteryEvent event, Long userId, String phoneNumber) {
        String content = buildMessage(event, userId, phoneNumber);
        discordWebhookSender.sendAsync(content);
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
