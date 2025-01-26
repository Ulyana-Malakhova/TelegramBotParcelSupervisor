package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public class TelegramBot extends TelegramLongPollingBot {

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
            String chatId = update.getMessage().getChatId().toString();

            // Ответ на сообщение
            String botResponse = "Вы ввели неверную команду, начните сообщение с символа '/'";

            // Отправка ответа
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(botResponse);

            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}
