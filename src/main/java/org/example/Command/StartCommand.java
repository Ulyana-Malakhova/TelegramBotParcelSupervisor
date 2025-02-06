package org.example.Command;

import org.example.Dto.UserDto;
import org.example.Service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.Collections;
@Component
public class StartCommand {
    private final UserServiceImpl userService;
    @Autowired
    public StartCommand(UserServiceImpl userService) {
        this.userService = userService;
    }

    public SendMessage execute(Update update) {
        String chatId = update.getMessage().getChatId().toString();
        // Отправляем сообщение с кнопкой для запроса номера телефона
        return sendPhoneShareMessage(chatId);
    }

    private SendMessage sendPhoneShareMessage(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Здравствуйте! Пожалуйста, поделитесь своим номером телефона, чтобы продолжить.");

        // Создаем кнопку для отправки номера телефона
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        KeyboardButton phoneButton = new KeyboardButton("Поделиться номером");
        phoneButton.setRequestContact(true); // Запросить контакт
        KeyboardRow row = new KeyboardRow();
        row.add(phoneButton);

        keyboardMarkup.setKeyboard(Collections.singletonList(row));
        message.setReplyMarkup(keyboardMarkup);
        return message;
    }

    public void createUserWithPhone(Long userId, String userName, String userSurname, String userUsername, String phoneNumber, String email, String password) {
        UserDto user = new UserDto(userId, userName, userSurname, userUsername, phoneNumber, 2, email, password);
        userService.save(user);  // Добавляем пользователя в базу данных
    }
}
