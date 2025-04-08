package org.example;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
public class WebhookController {
    private final TelegramBot telegramBot;

    public WebhookController(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @PostMapping("/webhook")
    public void handleUpdate(@RequestBody Update update) {
        telegramBot.onWebhookUpdateReceived(update);
    }
}
