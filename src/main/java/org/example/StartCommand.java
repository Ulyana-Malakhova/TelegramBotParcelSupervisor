package org.example;

import org.example.Entity.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.Collections;

public class StartCommand {
    private final UserRepository userRepository;

    public StartCommand() {
        this.userRepository = new UserRepository();
    }

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

    public void createUserWithPhone(Long userId, String userName, String userSurname, String userUsername, String phoneNumber, String email, String password) {
        User user = new User(userId, userName, userSurname, userUsername, phoneNumber, 2, email, password);
        userRepository.addUser(user); // Добавляем пользователя в базу данных
    }
}
