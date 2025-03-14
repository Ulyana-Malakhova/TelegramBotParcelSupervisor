package org.example.Command;

import org.example.Service.UserServiceImpl;
import org.example.TelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class ViewAdminsCommand {
    private final UserServiceImpl userService;

    private final TelegramBot telegramBot;

    @Autowired
    public ViewAdminsCommand(UserServiceImpl userService, @Lazy TelegramBot telegramBot) {
        this.userService = userService;
        this.telegramBot = telegramBot;
    }

    public void execute(long chatId) {
        ByteArrayOutputStream excelFile;
        try {
            // Получаем администраторов
            excelFile = userService.exportAdminsToExcel();
            // После получения Excel-файла, отправляем его пользователю
            telegramBot.sendDocument(chatId, excelFile, "view_admins.xlsx");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
