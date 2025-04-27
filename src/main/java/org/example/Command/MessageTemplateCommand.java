package org.example.Command;

import org.example.Dto.MessageTemplateDto;
import org.example.Service.MessageTemplateServiceImpl;
import org.example.TelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;

/**
 * Обработка команд, связанных с шаблонами сообщений
 */
@Component
public class MessageTemplateCommand {
    /**
     * Сервис шаблонов сообщений
     */
    private final MessageTemplateServiceImpl messageTemplateServiceImpl;
    @Autowired
    public MessageTemplateCommand(MessageTemplateServiceImpl messageTemplateServiceImpl) {
        this.messageTemplateServiceImpl = messageTemplateServiceImpl;
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

    /**
     * Сохранение шаблонов сообщений из бд в мапу
     * @param map мапа для сохранения
     */
    public void getTemplates(HashMap<String, String> map){
        List<MessageTemplateDto> messageTemplateDtos = messageTemplateServiceImpl.findAll();
        for (MessageTemplateDto m: messageTemplateDtos){
            map.put(m.getEvent(), m.getText());
        }
    }
}
