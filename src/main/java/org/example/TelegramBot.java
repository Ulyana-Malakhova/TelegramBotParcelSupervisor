package org.example;

import org.example.Command.AboutCommand;
import org.example.Command.HelpCommand;
import org.example.Command.TrackingCommand;
import org.example.Dto.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.example.Command.StartCommand;

import java.util.HashMap;
import java.util.Map;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final HelpCommand helpCommand = new HelpCommand();
    private final AboutCommand aboutCommand = new AboutCommand();
    private final TrackingCommand trackingCommand = new TrackingCommand();
    private final StartCommand startCommand;
    private final Map<Long, String> userQuestions = new HashMap<>();
    @Autowired
    public TelegramBot(StartCommand startCommand) {
        this.startCommand = startCommand;
    }

    @Override
    public String getBotUsername() {
        return "parcel_supervisor_bot";
    }

    @Override
    public String getBotToken() {
        return "7073195789:AAGYAHgEncvlJ40xZBPWgiuQMXvizDgWdRs";
    }
    private final String questionAdmin = "Register_Admin";
    private final String questionEmail = "Register_Email";


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String userMessage = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();

            // Обработка команды /start
            if (userMessage.equals("/start")) {
                sendMessage(startCommand.execute(update));
                if (!startCommand.isAdministratorRegistered()){
                    sendResponse(chatId, "Администратор не найден. Для регистрации Вас как администратора, укажите " +
                            "значение токена телеграм-бота");
                    userQuestions.put(update.getMessage().getChatId(), questionAdmin);
                }
            }
            // Обработка команды /help
            else if (userMessage.equals("/help")) {
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
            else if (userQuestions.containsKey(update.getMessage().getChatId())){
                if (userQuestions.get(update.getMessage().getChatId())==questionAdmin){
                    if (userMessage!=getBotToken()){
                        sendResponse(chatId, "Токен введен неверно");
                        userQuestions.remove(update.getMessage().getChatId());
                    }
                    else{
                        sendResponse(chatId, "Введите почту, на которую будет выслан пароль");
                        userQuestions.remove(update.getMessage().getChatId());
                        userQuestions.put(update.getMessage().getChatId(), questionEmail);
                    }
                }
                else if (userQuestions.get(update.getMessage().getChatId())==questionEmail){
                    UserDto userDto = UserDto.builder().id(update.getMessage().getChatId()).email(userMessage).build();
                    startCommand.createAdminUser(userDto);
                    sendResponse(chatId, "Пароль отправлен на почту");
                    userQuestions.remove(update.getMessage().getChatId());
                }
            }
            else {
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
        boolean newUser = startCommand.createUserWithPhone(userId, userName, userSurname, userUsername1, phoneNumber, null, null);
        if (newUser){
            sendResponseAndDeleteKeyboard(chatId, "Спасибо за предоставление вашего номера телефона!");
            sendResponse(chatId, helpCommand.getHelpMessage());
        }
        else {
            sendResponse(chatId,"Произошла ошибка, пожалуйста попробуйте снова поделиться номером телефона");
        }
    }
    private void sendResponseAndDeleteKeyboard(String chatId, String messageText){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);

        // Удаляем кнопку
        ReplyKeyboardRemove keyboardRemove = new ReplyKeyboardRemove();
        keyboardRemove.setRemoveKeyboard(true);
        message.setReplyMarkup(keyboardRemove);
        sendMessage(message);
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
