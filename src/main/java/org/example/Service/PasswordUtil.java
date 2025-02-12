package org.example.Service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Класс шифрования паролей
 */
public class PasswordUtil {
    /**
     * Объект хеширования паролей
     */
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Хеширование пароля
     * @param password начальный пароль
     * @return хештрованный пароль
     */

    public static String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * Проверка на соответствие пароля и хешированного пароля
     * @param password пароль
     * @param hashedPassword хешированный пароль
     * @return true - пароли совпадают, иначе - false
     */
    public static boolean checkPassword(String password, String hashedPassword) {
        return passwordEncoder.matches(password, hashedPassword);
    }
}
