package org.example.Command;

import org.example.Dto.MessageTemplateDto;
import org.example.Service.MessageTemplateServiceImpl;
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
    private final MessageTemplateServiceImpl messageTemplateServiceImpl;
    /**
     * Объект телеграм-бота
     */
    private final TelegramBot telegramBot;

    public MessageTemplateCommand(MessageTemplateServiceImpl messageTemplateServiceImpl, @Lazy TelegramBot telegramBot) {
        this.messageTemplateServiceImpl = messageTemplateServiceImpl;
        this.telegramBot = telegramBot;
    }

    /**
     * Получение excel-файла с шаблонами сообщений
     */
    public ByteArrayOutputStream sendTemplates() throws Exception {
        return messageTemplateServiceImpl.exportToExcel();
    }

    /**
     * Обновление информации о шаблоне
     * @param messageTemplateDto dto-объект шаблона сообщения
     * @throws Exception не найден пользователь
     */
    public void update(MessageTemplateDto messageTemplateDto) throws Exception {
        messageTemplateServiceImpl.save(messageTemplateDto);
    }

    /**
     * Поиск шаблона сообщения по id
     * @param id id шаблона
     * @return dto-объект шаблона сообщения
     */
    public MessageTemplateDto findById(Long id){
        return messageTemplateServiceImpl.get(id);
    }

    /**
     * Поиск шаблона сообщения по событию
     * @param event строка-событие
     * @return dto-объект шаблона сообщения
     */
    public MessageTemplateDto findByEvent(String event){
        return messageTemplateServiceImpl.findByEvent(event);
    }
}
