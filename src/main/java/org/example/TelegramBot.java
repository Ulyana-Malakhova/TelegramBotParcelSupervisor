package org.example;

import org.example.Command.AboutCommand;
import org.example.Command.HelpCommand;
import org.example.Command.TrackingCommand;
import org.example.Dto.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.example.Command.StartCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final BotProperties botProperties;
    private final HelpCommand helpCommand = new HelpCommand();
    private final AboutCommand aboutCommand = new AboutCommand();
    private final TrackingCommand trackingCommand = new TrackingCommand();
    private final StartCommand startCommand;
    /**
     * Мапа для хранения id чата и вопросов, ожидающих ответ
     */
    private final Map<Long, String> userQuestions = new HashMap<>();
    /**
     * Вопрос-ожидание токена
     */
    private final String questionToken = "Wait_Token";
    /**
     * Вопрос-ожидание электронной почты
     */
    private final String questionEmail = "Register_Email";
    /**
     * Регулярное выражение для проверки почты
     */
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    @Autowired
    public TelegramBot(StartCommand startCommand, BotProperties botProperties) {
        this.startCommand = startCommand;
        this.botProperties = botProperties;
    }

    @Override
    public String getBotUsername() {
        return botProperties.username;
    }

    @Override
    public String getBotToken() {
        return botProperties.token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String userMessage = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();

            // Обработка команды /start
            if (userMessage.equals("/start")) {
                sendMessage(startCommand.execute(update));
            }
            // Обработка команды /help
            else if (userMessage.equals("/help")) {
                sendResponse(chatId, helpCommand.getHelpMessage());
            }
            // Обработка команды /about
            else if (userMessage.equals("/about")) {
                sendResponse(chatId, aboutCommand.getAboutMessage());
            }   //обработка команд /track и /history
            else if (userMessage.startsWith("/track") || userMessage.startsWith("/history")) {
                String[] parts = userMessage.split(" ");
                if (parts.length > 1) {
                    if (userMessage.startsWith("/track")) sendResponse(chatId, trackingCommand.getTrackingMessage(parts[1]));
                    else sendResponse(chatId, trackingCommand.getHistoryMessage(parts[1]));
                } else {
                    sendResponse(chatId, "Пожалуйста, укажите номер отслеживания.");
                }
            }
            else if (userQuestions.containsKey(update.getMessage().getChatId())){   //если есть вопрос, на который бот ожидает ответ
                if (userQuestions.get(update.getMessage().getChatId()).equals(questionToken)){  //если ожидается токен
                    if (!userMessage.equals(getBotToken())){    //и токен введен неверно
                        sendResponse(chatId, "Токен введен неверно");
                        userQuestions.remove(update.getMessage().getChatId());
                    }
                    else{   //если токен введен верно, бот просит ввести почту
                        sendResponse(chatId, "Введите почту, на которую будет выслан пароль");
                        userQuestions.remove(update.getMessage().getChatId());
                        userQuestions.put(update.getMessage().getChatId(), questionEmail);
                    }
                }
                else if (userQuestions.get(update.getMessage().getChatId()).equals(questionEmail)){ //если бот ожидает ввод почты
                    if (isValidEmail(userMessage)) {    //и почта введена по шаблону
                        try {
                            sendResponse(chatId, "Подождите...");
                            UserDto userDto = UserDto.builder().id(update.getMessage().getChatId()).email(userMessage).build();
                            if (startCommand.updateAdminUser(userDto))  //меняем данные о пользователе
                                sendResponse(chatId, "Пароль отправлен на почту");
                            else
                                sendResponse(chatId, "Данные о вас не найдены в системе, пожалуйста, введите команду /start");
                            userQuestions.remove(update.getMessage().getChatId());
                        }catch (Exception e){
                            sendResponse(chatId,"Произошла ошибка: "+e.getMessage());
                        }
                    }
                    else {
                        sendResponse(chatId, "Неправильный формат электронной почты. Введите почту правильно");
                    }
                }
            }
            else {
                // Логика ответа на другие сообщения
                String botResponse = "Вы ввели неверную команду, начните сообщение с символа '/'";
                sendResponse(chatId, botResponse);
            }
        } else if (update.hasMessage() && update.getMessage().hasContact()) {
            handleContactUpdate(update);
        }
    }

    /**
     * Проверка введенной строки на соответствие шаблону электронной почты
     * @param email введенная строка почты
     * @return true - строка является электронной почтой, иначе false
     */
    private boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void handleContactUpdate(Update update) {
        String chatId = update.getMessage().getChatId().toString();
        String phoneNumber = update.getMessage().getContact().getPhoneNumber();

        Long userId = update.getMessage().getFrom().getId();
        String userName = update.getMessage().getFrom().getFirstName();
        String userSurname = update.getMessage().getFrom().getLastName();
        String userUsername = update.getMessage().getFrom().getUserName();
        if (userUsername != null) {
            userUsername = '@' + userUsername;
        }
        try {
            // Создаем пользователя с номером телефона
            boolean newUser = startCommand.createUserWithPhone(userId, userName, userSurname, userUsername, phoneNumber, null, null);
            if (newUser) {
                sendResponseAndDeleteKeyboard(chatId, "Спасибо за предоставление вашего номера телефона!");
                sendResponse(chatId, helpCommand.getHelpMessage());
                    if (!startCommand.isAdministratorRegistered()) {
                        sendResponse(chatId, "Администратор не найден. Для регистрации Вас как администратора, укажите " +
                                "значение токена телеграм-бота");
                        userQuestions.put(update.getMessage().getChatId(), questionToken);
                    }
            } else {
                sendResponse(chatId, "Произошла ошибка, пожалуйста, попробуйте снова поделиться номером телефона");
            }
        }catch (Exception e){
            sendResponse(chatId,"Произошла ошибка: "+e.getMessage());
        }
    }
    private void sendResponseAndDeleteKeyboard(String chatId, String messageText){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);

        // Удаляем кнопку
        ReplyKeyboardRemove keyboardRemove = new ReplyKeyboardRemove();
        keyboardRemove.setRemoveKeyboard(true);
        message.setReplyMarkup(keyboardRemove);
        sendMessage(message);
    }
    private void sendResponse(String chatId, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);
        sendMessage(message);
    }

    public void sendMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
