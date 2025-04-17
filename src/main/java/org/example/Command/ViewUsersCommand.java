package org.example.Command;

import org.example.AppConstants;
import org.example.Dto.UserDto;
import org.example.Entity.User;
import org.example.Service.UserServiceImpl;
import org.example.TelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Component
public class ViewUsersCommand {
    private final UserServiceImpl userService;

    private final TelegramBot telegramBot;

    @Autowired
    public ViewUsersCommand(UserServiceImpl userService, @Lazy TelegramBot telegramBot) {
        this.userService = userService;
        this.telegramBot = telegramBot;
    }

    /**
     * Получение списка обычных пользователей
     * @return dto-список пользователей
     * @throws Exception не найдена сущность статуса пользователя
     */
    public List<UserDto> getUsers() throws Exception {
        return userService.findByStatus(AppConstants.STATUS_USER);
    }

    public void execute(long chatId) {
        ByteArrayOutputStream excelFile;
        try {
            // Получаем активных пользователей
            excelFile = userService.exportActiveUsersToExcel();
            // После получения Excel-файла, отправляем его пользователю
            telegramBot.sendDocument(chatId, excelFile, "view_users.xlsx");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
