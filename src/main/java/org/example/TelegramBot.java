package org.example;

import org.example.command.TrackingCommand;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public class TelegramBot extends TelegramLongPollingBot {
    private final HelpCommand helpCommand = new HelpCommand();
    private final AboutCommand aboutCommand = new AboutCommand();
    private final TrackingCommand trackingCommand = new TrackingCommand();
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

            // Обработка команды /help
            if (userMessage.equals("/help")) {
                sendResponse(chatId, helpCommand.getHelpMessage());
            }
            // Обработка команды /about
            else if (userMessage.equals("/about")) {
                sendResponse(chatId, aboutCommand.getAboutMessage());
            }   //обработка команд /track и /history
            else if (userMessage.startsWith("/track") || userMessage.startsWith("/history")) {
                String[] parts = userMessage.split(" ");
                if (parts.length > 1) {
                    if (userMessage.startsWith("/track")) sendResponse(chatId, trackingCommand.getTrackingMessage(parts[1]));
                    else sendResponse(chatId, trackingCommand.getHistoryMessage(parts[1]));
                } else {
                    sendResponse(chatId, "Пожалуйста, укажите номер отслеживания.");
                }
            }
            else {
                // Логика ответа на другие сообщения
                String botResponse = "Вы ввели неверную команду, начните сообщение с символа '/'";
                sendResponse(chatId, botResponse);
            }
        }
    }

    private void sendResponse(String chatId, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
