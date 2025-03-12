package org.example.Dto;

import jakarta.persistence.*;
import lombok.*;
import org.example.Entity.User;

import java.util.Date;

/**
 * dto-объект шаблона сообщения
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageTemplateDto {
    /**
     * id сообщения
     */
    private Long id;
    /**
     * Текст сообщения
     */
    private String text;
    /**
     * Дата последнего изменения
     */
    private Date editDate;
    /**
     * Событие, к которому привязано сообщение
     */
    private String event;
    /**
     * id пользователя-автора последнего изменения
     */
    private Long idAuthorUser;
}
