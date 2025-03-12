package org.example.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

/**
 * Сущность шаблона сообщения
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "\"Message_Template\"", schema = "public")
public class MessageTemplate {
    /**
     * id сообщения
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"Id_Template\"")
    private Long id;
    /**
     * Текст сообщения
     */
    @Column(name = "\"Text_Message_Template\"")
    private String text;
    /**
     * Дата последнего изменения
     */
    @Column(name = "\"Last_Edit_Date\"")
    private Date editDate;
    /**
     * Событие, к которому привязано сообщение
     */
    @Column(name = "\"Event\"")
    private String event;
    /**
     * Пользователь-автор последнего изменения
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "\"Author_Last_Edit\"")
    private User authorUser;
}
