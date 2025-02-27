package org.example;

import org.apache.commons.lang3.RandomUtils;
import org.example.Command.AboutCommand;
import org.example.Command.HelpCommand;
import org.example.Command.TrackingCommand;
import org.example.Dto.MessageDto;
import org.example.Command.*;
import org.example.Dto.PackageDto;
import org.example.Dto.UserDto;
import org.example.Service.MessageServiceImpl;
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final BotProperties botProperties;
    private final HelpCommand helpCommand = new HelpCommand();
    private final AboutCommand aboutCommand = new AboutCommand();
    private final TrackingCommand trackingCommand = new TrackingCommand();
    private final StartCommand startCommand;
    private final MessageServiceImpl messageService;
    private final PackageCommand packageCommand;
    /**
     * Мапа для хранения id чата и вопросов, ожидающих ответ
     */
    private final Map<Long, String> userQuestions = new HashMap<>();
    /**
     * Мапа для сохранения промежуточной информации о посылках
     */
    private final Map<Long, PackageDto> userPackage = new HashMap<>();
    /**
     * Мапа для сохранения промежуточной информации о посылках при изменении статуса отслеживания
     */
    private final Map<Long, PackageDto> userPackageTrackingStatus = new HashMap<>();
    /**
     * Вопрос-ожидание токена
     */
    private final String questionToken = "Wait_Token";
    /**
     * Вопрос-ожидание электронной почты
     */
    private final String questionEmail = "Register_Email";
    /**
     * Вопрос о получении уведомлений
     */
    private final String questionNotification = "Вы хотели бы получать уведомления по этой посылке?";
    /**
     * Утвердительный ответ
     */
    private final String answerYes = "Да";
    /**
     * Отрицательный ответ
     */
    private final String answerNo = "Нет";
    /**
     * Вопрос о роли пользователя в контексте отправления
     */
    private final String questionRole = "Для учета статистики, пожалуйста, укажите, " +
            "являетесь вы отправителем или получателем этой посылки";
    /**
     * Ответ - роль отправителя
     */
    private final String answerSender = "Отправитель";
    /**
     * Ответ - роль получателя
     */
    private final String answerRecipient = "Получатель";
    /**
     * Регулярное выражение для проверки почты
     */
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    @Autowired
    public TelegramBot(StartCommand startCommand, BotProperties botProperties,MessageServiceImpl messageService, PackageCommand packageCommand) {
        this.startCommand = startCommand;
        this.botProperties = botProperties;
        this.messageService=messageService;
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
            Long id = update.getMessage().getChatId();
            String userMessage = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();
            long messageDate = update.getMessage().getDate();
            Long userId = update.getMessage().getFrom().getId();
            Date dateUserMessage = new Date(messageDate * 1000L);
            MessageDto messageDto = new MessageDto(RandomUtils.nextLong(0L, 9999L), userMessage, dateUserMessage, userId);
            messageService.save(messageDto);
            try {
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
                    processingTrack(userMessage, id);
                } else if (userMessage.equals("/saved_parcels")) {
                    sendResponse(chatId, packageCommand.getSavedTrackNumbers(id));
                } else if (userMessage.startsWith("/delete_name")) {
                    processingDeleteName(userMessage, id);
                } else if (userMessage.startsWith("/add_name")) {
                    processingAddName(userMessage, id);
                } else if (userMessage.startsWith("/traceability_track")) {
                    processingTraceability(userMessage, id);
                } else if (userQuestions.containsKey(id)) {   //если есть вопрос, на который бот ожидает ответ
                    processingQuestion(userMessage, id);
                } else if (userPackage.containsKey(id)) {   //если есть промежуточные данные о посылке
                    processingPackage(userMessage, id);
                } else if (userPackageTrackingStatus.containsKey(id)) {
                    processingStatusChange(userMessage, id);
                } else {
                    // Логика ответа на другие сообщения
                    String botResponse = "Вы ввели неверную команду, начните сообщение с символа '/'";
                    sendResponse(chatId, botResponse);
                }
            } catch (Exception e) {
                sendResponse(chatId, "Произошла ошибка: " + e.getMessage());
            }
        } else if (update.hasMessage() && update.getMessage().hasContact()) {
            handleContactUpdate(update);
        }
    }

    /**
     * Проверка введенной строки на соответствие шаблону электронной почты
     *
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

    /**
     * Обработка команд /track и /history
     * @param userMessage полученное сообщение
     * @param id id пользователя
     */
    private void processingTrack(String userMessage, Long id){
        int spaceIndex = userMessage.indexOf(" ");
        if (spaceIndex != -1) { //если после команды есть текст
            String track = userMessage.substring(spaceIndex + 1);
            PackageDto receivedNumber = packageCommand.findByName(id, track);   //проверяем, является ли текст именем
            if (receivedNumber!=null) track = receivedNumber.getTrackNumber();   //если не имя - ожидаем, что это трек-номер
            if (userMessage.startsWith("/track")) sendResponse(id.toString(), trackingCommand.getTrackingMessage(track));
            else sendResponse(id.toString(), trackingCommand.getHistoryMessage(track));
        } else {
            sendResponse(id.toString(), "Пожалуйста, укажите номер отслеживания или существующее имя.");
        }
    }

    /**
     * Обработка команды /delete_name
     * @param userMessage полученное сообщение
     * @param id id пользователя
     * @throws Exception если посылки с переданным именем не существует
     */
    private void processingDeleteName(String userMessage, Long id) throws Exception {
        int spaceIndex = userMessage.indexOf(" ");
        if (spaceIndex != -1) {
            packageCommand.deleteNameTrackNumber(id, userMessage.substring(spaceIndex + 1).toLowerCase());
            sendResponse(id.toString(), "Имя удалено");
        } else {
            sendResponse(id.toString(), "Пожалуйста, укажите удаляемое имя.");
        }
    }

    /**
     * Обработка команды /add_name для уже сохраненной посылки
     * @param userMessage полученное сообщение
     * @param id id пользователя
     * @throws Exception если имя посылки уже используется
     */
    private void processingAddName(String userMessage, Long id) throws Exception {
        int spaceIndex = userMessage.indexOf(" ");
        if (spaceIndex != -1) {
            String trackName = userMessage.substring(spaceIndex + 1);   //отделяем имя и трек-номер от команды
            spaceIndex = trackName.indexOf(" ");
            if (spaceIndex != -1 && //если передано и имя, и трек номер, формат которого правильный
                    trackingCommand.serviceDefinition(trackName.substring(0, spaceIndex)) != null) {
                if (packageCommand.findByName(id,   //проверяем, добавлял ли пользователь уже такое имя посылке
                        trackName.substring(spaceIndex + 1).toLowerCase()) != null)
                    sendResponse(id.toString(), "Такое имя посылки уже создано");
                else {  //иначе проверяем, есть ли уже данные о посылке для этого пользователя
                    PackageDto packageDto = packageCommand.findByTrack(id,
                            trackName.substring(0, spaceIndex));
                    if (packageDto != null) {   //если данные есть - меняем полученный DTO-объект
                        packageDto.setNamePackage(trackName.substring(spaceIndex + 1).toLowerCase());
                        packageCommand.addNameTrackNumber(packageDto);
                        sendResponse(id.toString(), "Имя сохранено.");

                    } else {    //данных нет - создаем новый DTO-объект
                        packageDto = PackageDto.builder().idUser(id).
                                namePackage(trackName.substring(spaceIndex + 1).toLowerCase())
                                .trackNumber(trackName.substring(0, spaceIndex)).build();
                        userPackage.put(id, packageDto);
                        sendQuestion(id, questionNotification, answerYes, answerNo);    //задаем вопрос об отслеживании
                    }
                }
            } else sendResponse(id.toString(), "Пожалуйста, укажите правильный трек-номер и имя для него.");
        } else {
            sendResponse(id.toString(), "Пожалуйста, укажите трек-номер и имя.");
        }
    }

    /**
     * Обработка команды /add_name для новой посылки
     * @param userMessage полученное сообщение
     * @param id id пользователя
     * @throws Exception если имя посылки уже используется
     */
    private void processingPackage(String userMessage, Long id) throws Exception {
        PackageDto packageDto = userPackage.get(id);
        if (packageDto.getNameTrackingStatus() == null) {   //если ответ об отслеживании еще не был получен
            //обрабатываем ответ
            if (userMessage.equals(answerYes)) packageDto.setNameTrackingStatus(AppConstants.TRACKED);
            if (userMessage.equals(answerNo)) packageDto.setNameTrackingStatus(AppConstants.NO_TRACKED);
            if (packageDto.getNameTrackingStatus() != null) {   //если ответ об отслеживании получен
                userPackage.put(id, packageDto);
                sendQuestion(id, questionRole, answerSender, answerRecipient);  //задаем вопрос о роли
            }
        } else if (packageDto.getNameRole() == null && userMessage.equals(answerSender) ||
                userMessage.equals(answerRecipient)) {  //если ожидаем ответ о роли - сохраняем значение роли
            packageDto.setNameRole(userMessage);
            trackingCommand.updateParcelDetails(packageDto);
            packageCommand.addNameTrackNumber(packageDto);  //сохраняем данные посылки
            userPackage.remove(id);
            sendResponseAndDeleteKeyboard(id.toString(), "Имя сохранено.");
        }
    }

    /**
     * Регистрация первого администратора
     * @param userMessage полученное сообщение
     * @param id id пользователя
     * @throws Exception если не удалось получить сущность статуса
     */
    private void processingQuestion(String userMessage, Long id) throws Exception {
        String idString = id.toString();
        if (userQuestions.get(id).equals(questionToken)) {  //если ожидается токен
            if (!userMessage.equals(getBotToken())) {    //и токен введен неверно
                sendResponse(idString, "Токен введен неверно");
                userQuestions.remove(id);
            } else {   //если токен введен верно, бот просит ввести почту
                sendResponse(idString, "Введите почту, на которую будет выслан пароль");
                userQuestions.remove(id);
                userQuestions.put(id, questionEmail);
            }
        } else if (userQuestions.get(id).equals(questionEmail)) { //если бот ожидает ввод почты
            if (isValidEmail(userMessage)) {    //и почта введена по шаблону
                sendResponse(idString, "Подождите...");
                UserDto userDto = UserDto.builder().id(id).email(userMessage).build();
                if (startCommand.updateAdminUser(userDto))  //меняем данные о пользователе
                    sendResponse(idString, "Пароль отправлен на почту");
                else
                    sendResponse(idString, "Данные о вас не найдены в системе, пожалуйста, введите команду /start");
                userQuestions.remove(id);
            } else {
                sendResponse(idString, "Неправильный формат электронной почты. Введите почту правильно");
            }
        }
    }

    /**
     * Обработка команды для изменения статуса отслеживания
     * @param userMessage полученное сообщение
     * @param id id пользователя
     */
    private void processingTraceability(String userMessage, Long id){
        int spaceIndex = userMessage.indexOf(" ");
        if (spaceIndex != -1) { //если после команды есть текст
            String track = userMessage.substring(spaceIndex + 1);
            PackageDto packageDto = packageCommand.findByName(id, track);   //ищем посылку по имени
            if (packageDto==null) packageDto = packageCommand.findByTrack(id, track);   //если посылку не нашли - ищем по трек-номеру
            if (packageDto==null && trackingCommand.serviceDefinition(track)!=null) {   //если посылка в бд не найдена, но в команде передан трек-номер
                packageDto = PackageDto.builder().idUser(id).trackNumber(track).build();
                sendQuestion(id, "Данные об отслеживании посылки не найдены. " +
                        "Хотите получать уведомления о статусе посылки?", answerYes, answerNo);
                userPackageTrackingStatus.put(id, packageDto);
            }
            else if (packageDto==null && trackingCommand.serviceDefinition(track)==null){   //если посылка не найдена, и передан не трек-номер
                sendResponse(id.toString(), "Пожалуйста, укажите трек-номер или существующее имя.");
            }
            else if (packageDto!=null){ //если посылка найдена
                if (packageDto.getNameTrackingStatus().equals(AppConstants.DELIVERED)) //и уже доставлена
                    sendResponse(id.toString(), "Посылка доставлена, поменять статус отслеживания нельзя.");
                else if (packageDto.getNameTrackingStatus().equals(AppConstants.CANCELED))
                    sendResponse(id.toString(), "Посылка отменена, поменять статус отслеживания нельзя.");
                else {
                    sendQuestion(id, "Сейчас посылка " + packageDto.getNameTrackingStatus().toLowerCase() + "" +
                            ". Хотите поменять статус отслеживания на противоположный?", answerYes, answerNo);
                    userPackageTrackingStatus.put(id, packageDto);
                }
            }
        }else {
            sendResponse(id.toString(), "Пожалуйста, укажите трек-номер или существующее имя.");
        }
    }

    /**
     * Обработка ответа об изменении статуса отслеживания
     * @param userMessage полученное сообщение
     * @param id id пользователя
     * @throws Exception не найдена запись посылки или статуса
     */
    private void processingStatusChange(String userMessage, Long id) throws Exception {
        PackageDto packageDto = userPackageTrackingStatus.get(id);
        if (packageDto.getNameTrackingStatus()!=null) {  //если посылка уже есть в бд
            if (userMessage.equals(answerYes)){ //и ответ утвердительный - меняем статус на противоположный
                if (packageDto.getNameTrackingStatus().equals(AppConstants.TRACKED))
                    packageDto.setNameTrackingStatus(AppConstants.NO_TRACKED);
                else if (packageDto.getNameTrackingStatus().equals(AppConstants.NO_TRACKED))
                    packageDto.setNameTrackingStatus(AppConstants.TRACKED);
                packageCommand.changeTrackingStatus(packageDto);
                sendResponseAndDeleteKeyboard(id.toString(), "Изменения сохранены, посылка "+packageDto.getNameTrackingStatus().toLowerCase());
            }
        }
        else{   //если посылки в бд еще нет
            if (userMessage.equals(answerYes)){ //и ответ утвердительный
                packageDto.setNameTrackingStatus(AppConstants.TRACKED);
                userPackageTrackingStatus.remove(id);
                userPackage.put(id, packageDto);    //добавляем посылку в мапу для создания посылки
                sendQuestion(id, questionRole, answerSender, answerRecipient);  //задаем вопрос о роли
            }
        }
        if (userMessage.equals(answerNo)) {
            userPackageTrackingStatus.remove(id);
            sendResponseAndDeleteKeyboard(id.toString(), "Отмена изменений.");
        }
    }
    /**
     * Отправка пользователю вопроса и добавление кнопок-ответов
     * @param chatId id пользователя
     * @param question строка-вопрос
     * @param firstAnswer первый возможный ответ
     * @param secondAnswer второй возможный ответ
     */
    private void sendQuestion(long chatId, String question, String firstAnswer, String secondAnswer){
        SendMessage message = new SendMessage();    //формирование сообщения
        message.setChatId(String.valueOf(chatId));
        message.setText(question);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup(); //создание клавиатуры для взаимодействия с пользователем
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
