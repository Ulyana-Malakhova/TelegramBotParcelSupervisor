package org.example.Service;

import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * Сервис для отправки электронных писем
 */
@Service
public class EmailService {
    /**
     * Заголовок сообщения
     */
    private final String subject = "Пароль для доступа к режиму администратора";
    /**
     * Текст сообщения
     */
    private final String message = "Для входа в режим администратора введите пароль: ";
    /**
     * Длина пароля
     */
    private final int length = 20;
    /**
     * Количество заглавных букв в пароле
     */
    private final int countUpperCase = 2;
    /**
     * Количество цифр в пароле
     */
    private final int countDigit = 3;
    /**
     * Количество спец символов в пароле
     */
    private final int countSpecial = 1;
    private final JavaMailSender emailSender;
    @Autowired
    public EmailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    /**
     * Метод отправки электронного письма с паролем администратора
     * @param toAddress адрес электронной почты
     * @return отправленный пароль
     */
    public String sendPassword(String toAddress) {
        String password = generatePassword();
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(toAddress);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(message+password);
        emailSender.send(simpleMailMessage);
        return password;
    }
    /**
     * Генерация пароля пароля
     * @return сгенерированный пароль
     */
    private String generatePassword(){
        PasswordGenerator passwordGenerator = new PasswordGenerator();  //генератор паролей

        ArrayList<CharacterRule> rules = new ArrayList<>();
        rules.add(new CharacterRule(EnglishCharacterData.UpperCase, countUpperCase)); //2 заглавные буквы
        rules.add(new CharacterRule(EnglishCharacterData.LowerCase, length-countDigit-countSpecial-countUpperCase));    //14 строчных букв
        rules.add(new CharacterRule(EnglishCharacterData.Digit, countDigit));    //3 цифры
        CharacterRule specialCharacterRule = new CharacterRule(new CharacterData() {    //1 символ из списка
            @Override
            public String getErrorCode() {
                return "SAMPLE_ERROR_CODE";
            }

            @Override
            public String getCharacters() {
                return "-!?*+=/";
            }
        }, countSpecial);
        rules.add(specialCharacterRule);
        return passwordGenerator.generatePassword(length, rules);
    }
}
