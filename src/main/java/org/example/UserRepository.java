package org.example;

import org.example.Entity.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserRepository {
    private final DatabaseManager databaseManager;

    public UserRepository() {
        this.databaseManager = new DatabaseManager();
    }

    public void addUser(User user) {
        String sql = "INSERT INTO \"User\" (\"Id_User\", \"Name\", \"Surname\", \"Username\", \"Phone_Number\", \"Id_Status\", \"Email\", \"Password\") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = databaseManager.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, user.getId());
            statement.setString(2, user.getName());
            statement.setString(3, user.getSurname());
            statement.setString(4, user.getUsername());
            statement.setString(5, user.getPhoneNumber());
            statement.setInt(6, user.getIdStatus());
            statement.setString(7, user.getEmail());
            statement.setString(8, user.getPassword());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Ошибка добавления пользователя в базу данных: " + e.getMessage());
        }
    }
}
