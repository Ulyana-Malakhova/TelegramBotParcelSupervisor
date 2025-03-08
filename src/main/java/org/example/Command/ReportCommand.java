package org.example.Command;

import org.example.Service.PackageService;
import org.example.TelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class ReportCommand {
    private final PackageService packageService;

    private final TelegramBot telegramBot;

    @Autowired
    public ReportCommand(PackageService packageService, @Lazy TelegramBot telegramBot) {
        this.packageService = packageService;
        this.telegramBot = telegramBot;
    }

    public void execute(long chatId, String period) {
        ByteArrayOutputStream excelFile;
        try {
            // Получаем данные за выбранный период
            excelFile = packageService.exportPackageToExcel(period);
            // После получения Excel-файла, отправляем его пользователю
            telegramBot.sendDocument(chatId, excelFile, "reportParcels.xlsx");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

}
