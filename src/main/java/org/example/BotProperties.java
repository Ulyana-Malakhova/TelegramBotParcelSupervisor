package org.example;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Setter
@NoArgsConstructor
@ConfigurationProperties(prefix = "telegram.bot")
public class BotProperties {
    String username;
    String token;
}
