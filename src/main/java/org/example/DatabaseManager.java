package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String URL = "jdbc:postgresql://localhost:5432/bot_parcel_supervisor";
    private static final String USER = "postgres";
    private static final String PASSWORD = "30032003";

    public Connection connect() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Подключена база данных PostgreSQL.");
        } catch (SQLException e) {
            System.out.println("Ошибка подключения к PostgreSQL: " + e.getMessage());
        }
        return connection;
    }
}
