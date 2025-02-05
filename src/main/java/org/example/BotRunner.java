package org.example;

import org.example.Command.StartCommand;
import org.example.Repository.UserRepository;
import org.example.Service.UserService;
import org.example.Service.UserServiceImpl;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class BotRunner {
    static UserService userService
            ;
    static StartCommand startCommand;
    public static void main(String[] args) throws TelegramApiException {
        // Инициализация API Telegram Bots
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            botsApi.registerBot(new TelegramBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}