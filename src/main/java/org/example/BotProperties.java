package org.example;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Класс с данными бота из файла кофигурации
 */
@Component
@Setter
@NoArgsConstructor
@ConfigurationProperties(prefix = "telegram.bot")
public class BotProperties {
    /**
     * Имя бота
     */
    String username;
    /**
     * Токен бота
     */
    String token;
}
