package org.example.command;

/**
 * Перечисление почтовых сервисов
 */
public enum PostalService {
    RUSSIAN_POST("Почта России"),
    BOXBERRY("Boxberry"),
    DPD("DPD"),
    CSE("КСЭ");

    final String name;      // Название сервиса

    PostalService(String name) {
        this.name = name;
    }
    @Override
    public String toString() {
        return name;
    }
}
