package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public class TelegramBot extends TelegramLongPollingBot {
    private final HelpCommand helpCommand = new HelpCommand();
    private static TelegramBot instance;
    private final AboutCommand aboutCommand = new AboutCommand();
    private final StartCommand startCommand = new StartCommand();

    public static TelegramBot getInstance() {
        if (instance == null) {
            instance = new TelegramBot();
        }
        return instance;
    }

    @Override
    public String getBotUsername() {
        return "parcel_supervisor_bot";
    }

    @Override
    public String getBotToken() {
        return "7073195789:AAGYAHgEncvlJ40xZBPWgiuQMXvizDgWdRs";
    }


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String userMessage = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();

            // Обработка команды /start
            if (userMessage.equals("/start")) {
                startCommand.execute(update);
            }
            // Обработка команды /help
            else if (userMessage.equals("/help")) {
                sendResponse(chatId, helpCommand.getHelpMessage());
            }
            // Обработка команды /about
            else if (userMessage.equals("/about")) {
                sendResponse(chatId, aboutCommand.getAboutMessage());
            } else {
                // Логика ответа на другие сообщения
                String botResponse = "Вы ввели неверную команду, начните сообщение с символа '/'";
                sendResponse(chatId, botResponse);
            }
        } else if (update.hasMessage() && update.getMessage().hasContact()) {
            handleContactUpdate(update);
        }
    }

    private void handleContactUpdate(Update update) {
        String chatId = update.getMessage().getChatId().toString();
        String phoneNumber = update.getMessage().getContact().getPhoneNumber();

        Long userId = update.getMessage().getFrom().getId();
        String userName = update.getMessage().getFrom().getFirstName();
        String userSurname = update.getMessage().getFrom().getLastName();
        String userUsername = update.getMessage().getFrom().getUserName();
        String userUsername1;
        if (userUsername != null) {
            userUsername1 = '@' + userUsername;
        } else {
            userUsername1 = userUsername;
        }
        // Создаем пользователя с номером телефона
        startCommand.createUserWithPhone(userId, userName, userSurname, userUsername1, phoneNumber, null, null);
        sendResponse(chatId, "Спасибо за предоставление вашего номера телефона!");
    }

    private void sendResponse(String chatId, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);
        sendMessage(message);
    }

    public void sendMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
