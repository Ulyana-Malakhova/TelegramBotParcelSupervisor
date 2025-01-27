package org.example.command;

public enum PostalService {
    RUSSIAN_POST("Почта России"),
    BOXBERRY("Boxberry"),
    DPD("DPD"),
    CSE("КСЭ");

    String name;      // Название сервиса

    PostalService(String name) {
        this.name = name;
    }
    @Override
    public String toString() {
        return name;
    }
}
