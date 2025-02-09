package org.example.api;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Класс данных о местоположении
 */
@Setter
@Getter
@NoArgsConstructor
public class PackageLocation {
    /**
     * Дата получения информации
     */
    private Date date;
    /**
     * Сообщение (статус посылки)
     */
    private String message;
    /**
     * Текущее местоположение
     */
    private String location;
    /**
     * Формат вывода даты
     */
    private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Дата: ").append(format.format(date))
                .append(", статус: ").append(message);
        // Проверяем, не пустое ли местоположение
        if (location != null && !location.isEmpty()) {
            result.append(", местоположение: ").append(location);
        }
        return result.toString();
    }
}
