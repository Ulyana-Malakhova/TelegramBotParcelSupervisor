package org.example.Command;

import lombok.RequiredArgsConstructor;
import org.example.Dto.UserDto;
import org.example.Service.UserService;
import org.example.Service.UserServiceImpl;
import org.example.TelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.Collections;
@RequiredArgsConstructor
public class StartCommand {
    private final UserService userService;

    public void execute(Update update) {
        String chatId = update.getMessage().getChatId().toString();
        // Отправляем сообщение с кнопкой для запроса номера телефона
        sendPhoneShareMessage(chatId);
    }

    private void sendPhoneShareMessage(String chatId) {
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

        // Отправляем сообщение через TelegramBot
        TelegramBot.getInstance().sendMessage(message);
    }

    public UserDto createUserWithPhone(Long userId, String userName, String userSurname, String userUsername, String phoneNumber, String email, String password) {
        UserDto user = new UserDto(userId, userName, userSurname, userUsername, phoneNumber, 2, email, password);
        return userService.save(user);  // Добавляем пользователя в базу данных
    }
}
