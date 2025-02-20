package org.example.Command;

import org.example.Dto.UserDto;
import org.example.Service.EmailService;
import org.example.Service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.Collections;
@Component
public class StartCommand {
    private final UserServiceImpl userService;
    /**
     * Сервис для отправки писем на электронную почту
     */
    private final EmailService emailService;
    @Autowired
    public StartCommand(UserServiceImpl userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
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

    @Transactional
    public boolean createUserWithPhone(Long userId, String userName, String userSurname, String userUsername, String phoneNumber, String email, String password) throws Exception {
        UserDto user = new UserDto(userId, userName, userSurname, userUsername, phoneNumber, 2L, email, password);
        userService.save(user);  // Добавляем пользователя в базу данных
        return userService.isUserExist(userId);
    }

    /**
     * Проверка, есть ли зарегистрированный администратор
     * @return true - администратор есть, иначе - false
     */
    public boolean isAdministratorRegistered() throws Exception {
        return userService.isAdministratorRegistered();
    }

    /**
     * Обновления данных обычного пользователя
     * @param userDto дто пользователя
     * @return true - администратор успешно создан, иначе - false
     * @throws Exception не найдена сущность статуса
     */
    public boolean updateAdminUser(UserDto userDto) throws Exception {
        if (!userService.isUserExist(userDto.getId())) return false;    //если данных о пользователе нет
        else{   //данные есть - формируем и отправляем пароль
            String password = emailService.sendPassword(userDto.getEmail());
            userService.updateUserToAdmin(userDto, password);   //обновляем данные о пользователе
            return true;
        }
    }
}
