package org.example.api;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Класс данных о местоположении
 */
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

    public void setDate(Date date) {
        this.date = date;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public PackageLocation() {
    }

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
