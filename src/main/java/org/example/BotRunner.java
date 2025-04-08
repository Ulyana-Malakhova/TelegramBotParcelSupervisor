package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@SpringBootApplication
public class BotRunner {
    public static void main(String[] args) {
        SpringApplication.run(BotRunner.class, args);
    }
}