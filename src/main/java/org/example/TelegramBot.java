package org.example;

import org.example.Command.*;
import org.example.Dto.PackageDto;
import org.example.Dto.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private final PackageCommand packageCommand;
    /**
     * Мапа для хранения id чата и вопросов, ожидающих ответ
     */
    private final Map<Long, String> userQuestions = new HashMap<>();
    private final Map<Long, PackageDto> userPackage = new HashMap<>();
    /**
     * Вопрос-ожидание токена
     */
    private final String questionToken = "Wait_Token";
    /**
     * Вопрос-ожидание электронной почты
     */
    private final String questionEmail = "Register_Email";
    private final String questionNotification = "Вы хотели бы получать уведомления по этой посылке?";
    private final String answerYes = "Да";
    private final String answerNo = "Нет";
    private final String questionRole = "Для учета статистики, пожалуйста, укажите, " +
            "являетесь вы отправителем или получателем этой посылки";
    private final String answerSender = "Отправитель";
    private final String answerRecipient = "Получатель";
    /**
     * Регулярное выражение для проверки почты
     */
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    @Autowired
    public TelegramBot(StartCommand startCommand, BotProperties botProperties, PackageCommand packageCommand) {
        this.startCommand = startCommand;
        this.botProperties = botProperties;
        this.packageCommand = packageCommand;
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
                int spaceIndex = userMessage.indexOf(" ");
                if (spaceIndex != -1) {
                    String track = userMessage.substring(spaceIndex + 1);
                    String receivedNumber = packageCommand.findByName(update.getMessage().getChatId(), track);
                    if (receivedNumber!=null) track = receivedNumber;
                    if (userMessage.startsWith("/track")) sendResponse(chatId, trackingCommand.getTrackingMessage(track));
                    else sendResponse(chatId, trackingCommand.getHistoryMessage(track));
                } else {
                    sendResponse(chatId, "Пожалуйста, укажите номер отслеживания или существующее имя.");
                }
            }
            else if (userMessage.equals("/saved_parcels")){
                sendResponse(chatId, packageCommand.getNamesTrackNumbers(update.getMessage().getChatId()));
            }
            else if (userMessage.startsWith("/delete_name")){
                int spaceIndex = userMessage.indexOf(" ");
                if (spaceIndex != -1) {
                    try {
                        packageCommand.deleteNameTrackNumber(update.getMessage().getChatId(), userMessage.substring(spaceIndex + 1).toLowerCase());
                        sendResponse(chatId, "Имя удалено");
                    }catch (Exception e){
                        sendResponse(chatId,"Произошла ошибка: "+e.getMessage());
                    }
                }
                else {
                    sendResponse(chatId, "Пожалуйста, укажите удаляемое имя.");
                }
            }
            else if (userMessage.startsWith("/add_name")){
                int spaceIndex = userMessage.indexOf(" ");
                if (spaceIndex != -1) {
                    String trackName = userMessage.substring(spaceIndex + 1);
                    spaceIndex = trackName.indexOf(" ");
                    if (spaceIndex!=-1 && trackingCommand.serviceDefinition(trackName.substring(0, spaceIndex))!=null){
                        if (packageCommand.findByName(update.getMessage().getChatId(),
                                trackName.substring(spaceIndex+1).toLowerCase())!=null)
                            sendResponse(chatId, "Такое имя посылки уже создано");
                        else {
                            PackageDto packageDto = packageCommand.findByTrack(update.getMessage().getChatId(),
                                    trackName.substring(0, spaceIndex));
                            if (packageDto!=null){
                                packageDto.setNamePackage(trackName.substring(spaceIndex + 1).toLowerCase());
                                try {
                                    packageCommand.addNameTrackNumber(packageDto);
                                    sendResponse(chatId,"Имя сохранено.");
                                } catch (Exception e) {
                                    sendResponse(chatId,"Произошла ошибка: "+e.getMessage());
                                }

                            }
                            else {
                                packageDto = PackageDto.builder().idUser(update.getMessage().getChatId()).
                                        namePackage(trackName.substring(spaceIndex + 1).toLowerCase())
                                        .trackNumber(trackName.substring(0, spaceIndex)).build();
                                userPackage.put(update.getMessage().getChatId(), packageDto);
                                sendQuestion(update.getMessage().getChatId(), questionNotification, answerYes, answerNo);
                            }
                        }
                    }
                    else sendResponse(chatId, "Пожалуйста, укажите правильный трек-номер и имя для него.");
                }
                else {
                    sendResponse(chatId, "Пожалуйста, укажите трек-номер и имя.");
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
            else if (userPackage.containsKey(update.getMessage().getChatId())){
                PackageDto packageDto = userPackage.get(update.getMessage().getChatId());
                if (packageDto.getNameTrackingStatus()==null){
                    if (userMessage.equals(answerYes)) packageDto.setNameTrackingStatus("Отслеживается");
                    if (userMessage.equals(answerNo)) packageDto.setNameTrackingStatus("Не отслеживается");
                    if (packageDto.getNameTrackingStatus()!=null){
                        userPackage.put(update.getMessage().getChatId(), packageDto);
                        sendQuestion(update.getMessage().getChatId(), questionRole, answerSender, answerRecipient);
                    }
                }
                else if (packageDto.getNameRole()==null && userMessage.equals(answerSender) ||
                            userMessage.equals(answerRecipient)) {
                    packageDto.setNameRole(userMessage);
                    try {
                        packageCommand.addNameTrackNumber(packageDto);
                        userPackage.remove(update.getMessage().getChatId());
                        sendResponse(chatId,"Имя сохранено.");
                    } catch (Exception e) {
                        sendResponse(chatId,"Произошла ошибка: "+e.getMessage());
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
    private void sendQuestion(long chatId, String question, String firstAnswer, String secondAnswer){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(question);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton(firstAnswer));
        row.add(new KeyboardButton(secondAnswer));
        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);
        sendMessage(message);
    }
}
