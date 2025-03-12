package org.example.Command;

import org.example.Service.MessageTemplateService;
import org.example.TelegramBot;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Обработка команд, связанных с шаблонами сообщений
 */
@Component
public class MessageTemplateCommand {
    /**
     * Сервис шаблонов сообщений
     */
    private final MessageTemplateService messageTemplateService;
    /**
     * Объект телеграм-бота
     */
    private final TelegramBot telegramBot;

    public MessageTemplateCommand(MessageTemplateService messageTemplateService, @Lazy TelegramBot telegramBot) {
        this.messageTemplateService = messageTemplateService;
        this.telegramBot = telegramBot;
    }

    /**
     * Отправка excel-файла с шаблонами сообщений
     * @param id id пользователя, которому отправляем
     */
    public void sendTemplates(Long id){
        ByteArrayOutputStream excelFile;
        try {
            // Получаем шаблоны сообщений
            excelFile = messageTemplateService.exportToExcel();
            // После получения Excel-файла, отправляем его пользователю
            telegramBot.sendDocument(id, excelFile, "view_templates.xlsx");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
