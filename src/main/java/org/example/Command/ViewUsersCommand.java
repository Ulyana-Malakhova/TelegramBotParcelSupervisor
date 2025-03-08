package org.example.Command;

import org.example.Service.UserServiceImpl;
import org.example.TelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class ViewUsersCommand {
    private final UserServiceImpl userService;

    private final TelegramBot telegramBot;

    @Autowired
    public ViewUsersCommand(UserServiceImpl userService, @Lazy TelegramBot telegramBot) {
        this.userService = userService;
        this.telegramBot = telegramBot;
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
