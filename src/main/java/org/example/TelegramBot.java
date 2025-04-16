package org.example;

import org.apache.commons.lang3.RandomUtils;
import org.example.Command.AboutCommand;
import org.example.Command.HelpCommand;
import org.example.Command.TrackingCommand;
import org.example.Dto.MessageDto;
import org.example.Command.*;
import org.example.Dto.MessageTemplateDto;
import org.example.Dto.PackageDto;
import org.example.Dto.UserDto;
import org.example.Entity.User;
import org.example.Service.MessageServiceImpl;
import org.example.Service.PasswordUtil;
import org.example.Service.StatusServiceImpl;
import org.example.Service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    /**
     * Мапа для хранение шаблонов сообщений
     */
    private final HashMap<String, String> messageTemplate;
    private static final long THIRTY_MINUTES_IN_MILLIS = 1800;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final BotProperties botProperties;
    private final HelpCommand helpCommand = new HelpCommand();
    private final AboutCommand aboutCommand = new AboutCommand();
    private final TrackingCommand trackingCommand = new TrackingCommand();
    private final MessageTemplateCommand messageTemplateCommand;
    private final StartCommand startCommand;
    private final MessageServiceImpl messageService;
    private final PackageCommand packageCommand;
    private final ReportCommand reportCommand;
    private final ViewUsersCommand viewUsersCommand;
    private final ViewAdminsCommand viewAdminsCommand;
    private final ViewBlockedUsersCommand viewBlockedUsersCommand;
    private final UserServiceImpl userService;
    private final StatusServiceImpl statusService;
    private final UserDataCommand userDataCommand;
    private final BlockUserCommand blockUserCommand;
    private final UnblockUserCommand unblockUserCommand;
    /**
     * Множество id пользователей, находящихся в режиме администратора
     */
    private final Set<Long> authorizedAdmins = new HashSet<>();
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
     * Мапа для сохранения dto для проверки пароля администратора
     */
    private final Map<Long, UserDto> adminAuthDTO = new HashMap<>();
    /**
     * Мапа для хранения данных при изменении статуса пользователя
     */
    private final Map<Long, Map<Long, String>> userUpdateStatus = new HashMap<>();
    /**
     * Мапа для сохранения данных при изменении шаблона сообщения
     */
    private final Map<Long, MessageTemplateDto> updateTemplate = new HashMap<>();
    /**
     * Вопрос-ожидание токена
     */
    private final String questionToken = "Wait_Token";
    /**
     * Вопрос-ожидание электронной почты
     */
    private final String questionEmail = "Register_Email";
    /**
     * Утвердительный ответ
     */
    private final String answerYes = "Да";
    /**
     * Отрицательный ответ
     */
    private final String answerNo = "Нет";
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
    public TelegramBot(StartCommand startCommand, BotProperties botProperties, MessageTemplateCommand messageTemplateCommand,
                       MessageServiceImpl messageService, PackageCommand packageCommand,
                       ReportCommand reportCommand, ViewUsersCommand viewUsersCommand,
                       ViewAdminsCommand viewAdminsCommand, ViewBlockedUsersCommand viewBlockedUsersCommand,
                       UserServiceImpl userService, StatusServiceImpl statusService, UserDataCommand userDataCommand, BlockUserCommand blockUserCommand, UnblockUserCommand unblockUserCommand) {
        this.userService = userService;
        this.statusService = statusService;
        this.blockUserCommand = blockUserCommand;
        this.unblockUserCommand = unblockUserCommand;
        this.messageTemplate = new HashMap<>();
        this.startCommand = startCommand;
        this.botProperties = botProperties;
        this.messageTemplateCommand = messageTemplateCommand;
        this.messageService = messageService;
        this.packageCommand = packageCommand;
        this.userDataCommand = userDataCommand;
        this.reportCommand = reportCommand;
        this.viewUsersCommand = viewUsersCommand;
        this.viewAdminsCommand = viewAdminsCommand;
        this.viewBlockedUsersCommand = viewBlockedUsersCommand;
        messageTemplateCommand.getTemplates(messageTemplate);
        start();
    }

    /**
     * Запуск выполнения функции с определенным интервалом
     */
    public void start() {
        scheduler.scheduleAtFixedRate(this::checkUserMessages, 0, 3, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(this::monitorParcelStatus, 0, 1, TimeUnit.MINUTES);
    }

    /**
     * Выход из режима администратора для неактивных пользователей
     */
    private void checkUserMessages() {
        CompletableFuture.runAsync(() -> {
            Set<Long> usersToRemove = new HashSet<>();
            for (Long id : authorizedAdmins) {
                MessageDto messageDto = messageService.getLatest(id);
                if (messageDto == null ||
                        System.currentTimeMillis() - messageDto.getDate().getTime() > THIRTY_MINUTES_IN_MILLIS) {
                    usersToRemove.add(id);
                    sendResponse(id.toString(), getTemplate("exit"));
                }
            }
            authorizedAdmins.removeAll(usersToRemove);
        });
    }

    /**
     * Отправка уведомлений об изменении статуса посылки
     */
    private void monitorParcelStatus() {
        CompletableFuture.runAsync(() -> {
        try {
            List<PackageDto> trackingPackageDtos = packageCommand.getByStatus(AppConstants.TRACKED);
            for (PackageDto packageDto : trackingPackageDtos) {
                String status = packageDto.getLatestStatus();
                trackingCommand.updateParcelDetails(packageDto);
                if (packageDto.getLatestStatus() != null && !packageDto.getLatestStatus().equals(status)) {
                    String name;
                    if (packageDto.getNamePackage() != null) name = packageDto.getNamePackage();
                    else name = packageDto.getTrackNumber();
                    String message = "Отправление " + name + ": " + packageDto.getLatestStatus();
                    sendResponse(String.valueOf(packageDto.getIdUser()), message);
                    packageCommand.changeStatus(packageDto);
                }
            }
            }catch(Exception e){
                System.out.println(e.getMessage());
            }
        });
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
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            if (callbackData.startsWith("report_period_")) {
                String period = callbackData.split("_")[2];
                reportCommand.execute(chatId, period);
            }
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            Long id = update.getMessage().getChatId();
            String userMessage = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();
            long longChatId = update.getMessage().getChatId();
            long messageDate = update.getMessage().getDate();
            Long userId = update.getMessage().getFrom().getId();
            Date dateUserMessage = new Date(messageDate * 1000L);
            MessageDto messageDto = new MessageDto(RandomUtils.nextLong(0L, 9999L), userMessage, dateUserMessage, userId);
            User user = userService.findById(id);
            if (user==null || !user.getStatus().getStatusName().equals(AppConstants.STATUS_BLOCKED)) {
                try {
                    messageService.save(messageDto);
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
                    }
                    //обработка команд /track и /history
                    else if (userMessage.startsWith("/track") || userMessage.startsWith("/history")) {
                        processingTrack(userMessage, id);
                    } else if (userMessage.equals("/saved_parcels")) {
                        sendResponse(chatId, packageCommand.getSavedTrackNumbers(id));
                    } else if (userMessage.equals("/change_password") && authorizedAdmins.contains(id)) {
                        processingChangePassword(id);
                    } else if (userMessage.equals("/auth")) {
                        processingAuthorization(id);
                    } else if (userMessage.equals("/exit")) {
                        processingExit(id);
                    } else if (userMessage.startsWith("/delete_name")) {
                        processingDeleteName(userMessage, id);
                    } else if (userMessage.startsWith("/add_name")) {
                        processingAddName(userMessage, id);
                    } else if (userMessage.startsWith("/change_email") && authorizedAdmins.contains(id)) {
                        processingChangeEmail(userMessage, id);
                    } else if (userMessage.startsWith("/set_user_role") && authorizedAdmins.contains(id)) {
                        processingSetUserRole(userMessage, id);
                    } else if (userMessage.equals("/view_templates") && authorizedAdmins.contains(id)) {
                        ByteArrayOutputStream stream = messageTemplateCommand.sendTemplates();
                        sendDocument(id, stream, "view_templates.xlsx");
                    } else if (userMessage.startsWith("/set_template") && authorizedAdmins.contains(id)) {
                        processingSetTemplate(userMessage, id);
                    } else if (userMessage.startsWith("/traceability_track")) {
                        processingTraceability(userMessage, id);
                    } else if (userQuestions.containsKey(id)) {   //если есть вопрос, на который бот ожидает ответ
                        processingQuestion(userMessage, id);
                    } else if (userPackage.containsKey(id)) {   //если есть промежуточные данные о посылке
                        processingPackage(userMessage, id);
                    } else if (userPackageTrackingStatus.containsKey(id)) {
                        processingStatusChange(userMessage, id);
                    } else if (adminAuthDTO.containsKey(id)) {
                        passwordCheck(userMessage, id, update.getMessage().getMessageId());
                    } else if (userUpdateStatus.containsKey(id)) {
                        statusChange(userMessage, id);
                    } else if (updateTemplate.containsKey(id)) {
                        updateMessageTemplate(userMessage, id);
                    }
                    // Обработка команды /report
                    else if (userMessage.equals("/report")) {
                        reportOption(longChatId);
                    }
                    // Обработка команды /view_users
                    else if (userMessage.equals("/view_users")) {
                        viewUsersCommand.execute(longChatId);
                    }
                    // Обработка команды /view_blocked_users
                    else if (userMessage.equals("/view_blocked_users")) {
                        viewBlockedUsersCommand.execute(longChatId);
                    }
                    // Обработка команды /view_admins
                    else if (userMessage.equals("/view_admins")) {
                        viewAdminsCommand.execute(longChatId);
                    }
                    //обработка команды block_user
                    else if (userMessage.startsWith("/block_user") && authorizedAdmins.contains(id)) {
                        processingBlockUserId(userMessage, id);
                    }
                    //обработка команды unblock_user
                    else if (userMessage.startsWith("/unblock_user") && authorizedAdmins.contains(id)) {
                        processingUnblockUserId(userMessage, id);
                    }
                    else {
                        sendResponse(chatId, getTemplate("error_command"));
                    }
                } catch (Exception e) {
                    sendResponse(chatId, "Произошла ошибка: " + e.getMessage());
                }
            }
            else {
                sendResponse(chatId, "Вы заблокированы в данном боте");
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
                sendResponseAndDeleteKeyboard(chatId, getTemplate("phone"));
                sendResponse(chatId, helpCommand.getHelpMessage());
                if (!startCommand.isAdministratorRegistered()) {
                    sendResponse(chatId, getTemplate("reg_admin"));
                    userQuestions.put(update.getMessage().getChatId(), questionToken);
                }
            } else {
                sendResponse(chatId, getTemplate("error_phone"));
            }
        } catch (Exception e) {
            sendResponse(chatId, "Произошла ошибка: " + e.getMessage());
        }
    }

    private void sendResponseAndDeleteKeyboard(String chatId, String messageText) {
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
     *
     * @param userMessage полученное сообщение
     * @param id          id пользователя
     */
    private void processingTrack(String userMessage, Long id) {
        int spaceIndex = userMessage.indexOf(" ");
        if (spaceIndex != -1) { //если после команды есть текст
            String track = userMessage.substring(spaceIndex + 1);
            PackageDto receivedNumber = packageCommand.findByName(id, track);   //проверяем, является ли текст именем
            if (receivedNumber != null)
                track = receivedNumber.getTrackNumber();   //если не имя - ожидаем, что это трек-номер
            if (userMessage.startsWith("/track"))
                sendResponse(id.toString(), trackingCommand.getTrackingMessage(track));
            else sendResponse(id.toString(), trackingCommand.getHistoryMessage(track));
        } else {
            sendResponse(id.toString(), getTemplate("error_track"));
        }
    }

    /**
     * Обработка команды /delete_name
     *
     * @param userMessage полученное сообщение
     * @param id          id пользователя
     * @throws Exception если посылки с переданным именем не существует
     */
    private void processingDeleteName(String userMessage, Long id) throws Exception {
        int spaceIndex = userMessage.indexOf(" ");
        if (spaceIndex != -1) {
            packageCommand.deleteNameTrackNumber(id, userMessage.substring(spaceIndex + 1).toLowerCase());
            sendResponse(id.toString(), getTemplate("delete_name"));
        } else {
            sendResponse(id.toString(), getTemplate("error_delete_name"));
        }
    }

    /**
     * Обработка команды /add_name для уже сохраненной посылки
     *
     * @param userMessage полученное сообщение
     * @param id          id пользователя
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
                    sendResponse(id.toString(), getTemplate("name_exists"));
                else {  //иначе проверяем, есть ли уже данные о посылке для этого пользователя
                    PackageDto packageDto = packageCommand.findByTrack(id,
                            trackName.substring(0, spaceIndex));
                    if (packageDto != null) {   //если данные есть - меняем полученный DTO-объект
                        packageDto.setNamePackage(trackName.substring(spaceIndex + 1).toLowerCase());
                        packageCommand.addNameTrackNumber(packageDto);
                        sendResponse(id.toString(), getTemplate("save_name"));

                    } else {    //данных нет - создаем новый DTO-объект
                        packageDto = PackageDto.builder().idUser(id).
                                namePackage(trackName.substring(spaceIndex + 1).toLowerCase())
                                .trackNumber(trackName.substring(0, spaceIndex)).build();
                        userPackage.put(id, packageDto);
                        sendQuestion(id, getTemplate("notification"), answerYes, answerNo);    //задаем вопрос об отслеживании
                    }
                }
            } else sendResponse(id.toString(), getTemplate("error_add_name"));
        } else {
            sendResponse(id.toString(), getTemplate("error_no_track"));
        }
    }

    /**
     * Обработка команды /add_name для новой посылки
     *
     * @param userMessage полученное сообщение
     * @param id          id пользователя
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
                sendQuestion(id, getTemplate("role"), answerSender, answerRecipient);  //задаем вопрос о роли
            }
        } else if (packageDto.getNameRole() == null && userMessage.equals(answerSender) ||
                userMessage.equals(answerRecipient)) {  //если ожидаем ответ о роли - сохраняем значение роли
            packageDto.setNameRole(userMessage);
            try {
                trackingCommand.updateParcelDetails(packageDto);
            }catch (IOException | ParseException e){
                sendResponse(String.valueOf(id), "Данные о посылке не были найдены");
            }
            packageCommand.addNameTrackNumber(packageDto);  //сохраняем данные посылки
            userPackage.remove(id);
            sendResponseAndDeleteKeyboard(id.toString(), getTemplate("save_name"));
        }
    }

    /**
     * Регистрация первого администратора
     *
     * @param userMessage полученное сообщение
     * @param id          id пользователя
     * @throws Exception если не удалось получить сущность статуса
     */
    private void processingQuestion(String userMessage, Long id) throws Exception {
        String idString = id.toString();
        if (userQuestions.get(id).equals(questionToken)) {  //если ожидается токен
            if (!userMessage.equals(getBotToken())) {    //и токен введен неверно
                sendResponse(idString, getTemplate("error_token"));
                userQuestions.remove(id);
            } else {   //если токен введен верно, бот просит ввести почту
                sendResponse(idString, getTemplate("input_email"));
                userQuestions.remove(id);
                userQuestions.put(id, questionEmail);
            }
        } else if (userQuestions.get(id).equals(questionEmail)) { //если бот ожидает ввод почты
            if (isValidEmail(userMessage)) {    //и почта введена по шаблону
                sendResponse(idString, getTemplate("wait"));
                if (startCommand.updateAdminUser(id, userMessage)) {  //меняем данные о пользователе
                    sendResponse(idString, getTemplate("send"));
                    userQuestions.remove(id);
                }
                else
                    sendResponse(idString, getTemplate("error_user"));
            } else {
                sendResponse(idString, getTemplate("error_email"));
            }
        }
    }

    /**
     * Обработка команды для изменения статуса отслеживания
     *
     * @param userMessage полученное сообщение
     * @param id          id пользователя
     */
    private void processingTraceability(String userMessage, Long id) {
        int spaceIndex = userMessage.indexOf(" ");
        if (spaceIndex != -1) { //если после команды есть текст
            String track = userMessage.substring(spaceIndex + 1);
            PackageDto packageDto = packageCommand.findByName(id, track);   //ищем посылку по имени
            if (packageDto == null)
                packageDto = packageCommand.findByTrack(id, track);   //если посылку не нашли - ищем по трек-номеру
            if (packageDto == null && trackingCommand.serviceDefinition(track) != null) {   //если посылка в бд не найдена, но в команде передан трек-номер
                packageDto = PackageDto.builder().idUser(id).trackNumber(track).build();
                sendQuestion(id, getTemplate("quest_notif"), answerYes, answerNo);
                userPackageTrackingStatus.put(id, packageDto);
            } else if (packageDto == null && trackingCommand.serviceDefinition(track) == null) {   //если посылка не найдена, и передан не трек-номер
                sendResponse(id.toString(), getTemplate("error_track"));
            } else if (packageDto != null) { //если посылка найдена
                if (packageDto.getNameTrackingStatus().equals(AppConstants.DELIVERED)) //и уже доставлена
                    sendResponse(id.toString(), getTemplate("delivered"));
                else if (packageDto.getNameTrackingStatus().equals(AppConstants.CANCELED))
                    sendResponse(id.toString(), getTemplate("canceled"));
                else {
                    sendQuestion(id, "Сейчас посылка " + packageDto.getNameTrackingStatus().toLowerCase() + "" +
                            ". Хотите поменять статус отслеживания на противоположный?", answerYes, answerNo);
                    userPackageTrackingStatus.put(id, packageDto);
                }
            }
        } else {
            sendResponse(id.toString(), getTemplate("error_track"));
        }
    }

    /**
     * Обработка ответа об изменении статуса отслеживания
     *
     * @param userMessage полученное сообщение
     * @param id          id пользователя
     * @throws Exception не найдена запись посылки или статуса
     */
    private void processingStatusChange(String userMessage, Long id) throws Exception {
        PackageDto packageDto = userPackageTrackingStatus.get(id);
        userPackageTrackingStatus.remove(id);
        if (packageDto.getNameTrackingStatus() != null) {  //если посылка уже есть в бд
            if (userMessage.equals(answerYes)) { //и ответ утвердительный - меняем статус на противоположный
                if (packageDto.getNameTrackingStatus().equals(AppConstants.TRACKED))
                    packageDto.setNameTrackingStatus(AppConstants.NO_TRACKED);
                else if (packageDto.getNameTrackingStatus().equals(AppConstants.NO_TRACKED))
                    packageDto.setNameTrackingStatus(AppConstants.TRACKED);
                packageCommand.changeStatus(packageDto);
                sendResponseAndDeleteKeyboard(id.toString(), "Изменения сохранены, посылка " + packageDto.getNameTrackingStatus().toLowerCase());
            }
        } else {   //если посылки в бд еще нет
            if (userMessage.equals(answerYes)) { //и ответ утвердительный
                packageDto.setNameTrackingStatus(AppConstants.TRACKED);
                userPackage.put(id, packageDto);    //добавляем посылку в мапу для создания посылки
                sendQuestion(id, getTemplate("role"), answerSender, answerRecipient);  //задаем вопрос о роли
            }
        }
        if (userMessage.equals(answerNo)) {
            sendResponseAndDeleteKeyboard(id.toString(), getTemplate("cancel_change"));
        }
    }


    /**
     * Обработка команды изменения почты
     * @param userMessage полученное сообщение
     * @param id id пользователя
     * @throws Exception не найден статус пользователя
     */
    private void processingChangeEmail(String userMessage, Long id) throws Exception {
            int spaceIndex = userMessage.indexOf(" ");
            if (spaceIndex != -1 && isValidEmail(userMessage.substring(spaceIndex + 1))) { //если почта введена и соответствует шаблону
                if (userDataCommand.updateEmail(id, userMessage.substring(spaceIndex + 1)))    //если новую почту удалось сохранить
                    sendResponse(id.toString(), getTemplate("change_email"));
                else sendResponse(id.toString(), getTemplate("error_change_email"));
            } else {
                sendResponse(id.toString(), getTemplate("error_email"));
            }
    }

    /**
     * Обработка команды изменения почты
     * @param id id пользователя
     * @throws Exception не найден статус пользователя
     */
    private void processingChangePassword(Long id) throws Exception {
        sendResponse(id.toString(), getTemplate("wait"));
        if (userDataCommand.updatePassword(id)) sendResponse(id.toString(), getTemplate("send"));
        else sendResponse(id.toString(), getTemplate("error_change_password"));
    }

    /**
     * Обработка команды авторизации
     * @param id id пользователя
     */
    private void processingAuthorization(Long id){
        UserDto userDto = userDataCommand.getAdminDto(id);
        if (userDto==null) sendResponse(id.toString(), getTemplate("error_auth"));
        else{
            sendResponse(id.toString(), getTemplate("input_password"));
            adminAuthDTO.put(id, userDto);
        }
    }

    /**
     * Проверка правильности введенного пароля
     * @param userMessage полученное сообщение
     * @param id id пользователя
     * @param messageId id сообщения с паролем
     * @throws TelegramApiException ошибка при попытке удалить сообщение с паролем
     */
    private void passwordCheck(String userMessage, Long id, Integer messageId) throws TelegramApiException {
        UserDto userDto = adminAuthDTO.get(id);
        if (PasswordUtil.checkPassword(userMessage, userDto.getPassword())){
            sendResponse(id.toString(), getTemplate("auth"));
            authorizedAdmins.add(id);
            adminAuthDTO.remove(id);
        }
        else {
            sendResponse(id.toString(), getTemplate("error_password"));
            adminAuthDTO.remove(id);
        }
        execute(new DeleteMessage(id.toString(), messageId));
    }

    /**
     * Обработка выхода из режима администратора
     * @param id id пользователя
     */
    private void processingExit(Long id){
        if (!authorizedAdmins.contains(id)) sendResponse(id.toString(), getTemplate("error_exit"));
        else{
            authorizedAdmins.remove(id);
            sendResponse(id.toString(), getTemplate("exit"));
        }
    }

    /**
     * Обработка изменения роли другого пользователя
     * @param userMessage полученное сообщение
     * @param id id пользователя
     */
    private void processingSetUserRole(String userMessage, Long id){
        int spaceIndex = userMessage.indexOf(" ");
        if (spaceIndex != -1 && trackingCommand.isOnlyNumbers(userMessage.substring(spaceIndex + 1))) {
            Long idUser = Long.parseLong(userMessage.substring(spaceIndex + 1));
            String status = userDataCommand.getStatusUser(idUser);
            if (status == null) sendResponse(id.toString(), getTemplate("error_find_user"));
            else {
                HashMap<Long, String> idUserStatus = new HashMap<>();
                if (status.equals(AppConstants.STATUS_BLOCKED))
                    sendResponse(id.toString(), getTemplate("error_role_blocked"));
                else if (status.equals(AppConstants.STATUS_ADMIN)) {
                    idUserStatus.put(idUser, status);
                    userUpdateStatus.put(id, idUserStatus);
                    sendQuestion(id, getTemplate("role_admin"), answerYes, answerNo);
                }
                else if (status.equals(AppConstants.STATUS_USER)) {
                    idUserStatus.put(idUser, status);
                    userUpdateStatus.put(id, idUserStatus);
                    sendQuestion(id, getTemplate("role_user"), answerYes, answerNo);
                }
            }
        }
        else sendResponse(id.toString(), getTemplate("error_id"));
    }

    /**
     * Обработка команды обновления шаблона сообщения
     * @param userMessage полученное сообщение
     * @param id id пользователя
     */
    public void processingSetTemplate(String userMessage, Long id){
        int spaceIndex = userMessage.indexOf(" ");
        if (spaceIndex!=-1){
            String identifierTemplate = userMessage.substring(spaceIndex + 1);
            MessageTemplateDto messageTemplateDto = null;
            if (trackingCommand.isOnlyNumbers(identifierTemplate))
                messageTemplateDto=messageTemplateCommand.findById(Long.parseLong(identifierTemplate));
            else messageTemplateDto=messageTemplateCommand.findByEvent(identifierTemplate);
            if (messageTemplateDto==null){
                sendResponse(id.toString(), getTemplate("error_find_template"));
            }
            else {
                updateTemplate.put(id, messageTemplateDto);
                sendResponse(id.toString(), "Сейчас шаблон имеет следующий текст: "+messageTemplateDto.getText()
                +". Введите новый текст или отправьте \"Отмена\" для отмены изменений");
            }
        }
        else sendResponse(id.toString(), getTemplate("error_id_template"));
    }

    /**
     * Обновление шаблона сообщения
     * @param userMessage текст нового шаблона
     * @param id id пользователя
     * @throws Exception не найден пользователь
     */
    public void updateMessageTemplate(String userMessage, Long id) throws Exception {
        if (userMessage.equals("Отмена")){
            sendResponse(id.toString(), getTemplate("cancel_change"));
        }
        else{
            MessageTemplateDto messageTemplateDto = updateTemplate.get(id);
            messageTemplateDto.setText(userMessage);
            messageTemplateDto.setIdAuthorUser(id);
            messageTemplateDto.setEditDate(new Date());
            messageTemplateCommand.update(messageTemplateDto);
            sendResponse(id.toString(), getTemplate("update_template"));
            messageTemplate.put(messageTemplateDto.getEvent(), messageTemplateDto.getText());
        }
        updateTemplate.remove(id);
    }
    /**
     * Изменение статуса пользователя в зависимости от полученного ответа
     * @param userMessage полученное сообщение
     * @param id id пользователя
     * @throws Exception не найден статус или пользователь
     */
    private void statusChange(String userMessage, Long id) throws Exception {
        if (userMessage.equals(answerYes)){
            Map<Long, String> idUserStatus = userUpdateStatus.get(id);
            userUpdateStatus.remove(id);
            for (Long idUser: idUserStatus.keySet()){
                if (idUserStatus.get(idUser).equals(AppConstants.STATUS_ADMIN)){
                    adminAuthDTO.remove(idUser);
                    if (userDataCommand.updateAdminToUser(id)) {
                        sendResponseAndDeleteKeyboard(id.toString(), getTemplate("update_role"));
                        sendResponse(idUser.toString(), getTemplate("update_to_user"));
                    }
                    else sendResponseAndDeleteKeyboard(id.toString(), getTemplate("error_find_user"));
                }
                else if (idUserStatus.get(idUser).equals(AppConstants.STATUS_USER)){
                    sendResponseAndDeleteKeyboard(id.toString(), getTemplate("send_request"));
                    sendResponse(idUser.toString(), getTemplate("update_to_admin"));
                    userQuestions.put(idUser, questionEmail);
                }
            }

        }
        else if (userMessage.equals(answerNo)){
            userUpdateStatus.remove(id);
            sendResponseAndDeleteKeyboard(id.toString(), getTemplate("cancel_change"));
        }
    }
    /**
     * Отправка пользователю вопроса и добавление кнопок-ответов
     *
     * @param chatId       id пользователя
     * @param question     строка-вопрос
     * @param firstAnswer  первый возможный ответ
     * @param secondAnswer второй возможный ответ
     */
    private void sendQuestion(long chatId, String question, String firstAnswer, String secondAnswer) {
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

    /**
     * Создание кнопок и вопроса пользователю после команды report
     *
     * @param chatId id чата пользователя
     */
    private void reportOption(long chatId) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // Первая строка с кнопками
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createInlineKeyboardButton("1 день", "report_period_1day"));
        row1.add(createInlineKeyboardButton("1 неделя", "report_period_1week"));
        rows.add(row1);

        // Вторая строка с кнопками
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createInlineKeyboardButton("1 месяц", "report_period_1month"));
        row2.add(createInlineKeyboardButton("3 месяца", "report_period_3months"));
        rows.add(row2);

        // Третья строка с кнопкой
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createInlineKeyboardButton("6 месяцев", "report_period_6months"));
        rows.add(row3);

        keyboardMarkup.setKeyboard(rows);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(getTemplate("select_interval"));
        message.setReplyMarkup(keyboardMarkup);

        sendMessage(message);
    }

    /**
     * Создание инлайн кнопок
     *
     * @param text         текст для кнопок
     * @param callbackData данные для отправки боту
     * @return созданные кнопки
     */
    private InlineKeyboardButton createInlineKeyboardButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    /**
     * Отправка документа пользователю
     *
     * @param chatId     id пользователя
     * @param fileStream поток с файлом
     * @param fileName   название файла
     */
    public void sendDocument(long chatId, ByteArrayOutputStream fileStream, String fileName) {
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(chatId);

        InputFile inputFile = new InputFile(new ByteArrayInputStream(fileStream.toByteArray()), fileName);
        sendDocument.setDocument(inputFile);

        try {
            execute(sendDocument);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private String getTemplate(String event){
        return messageTemplate.getOrDefault(event, "Сообщение не найдено.");
    }

    /**
     * Обработка команды /block_user
     *
     * @param userMessage полученное сообщение
     * @param id          id администратора
     */
    private void processingBlockUserId(String userMessage, Long id) {
        int spaceIndex = userMessage.indexOf(" ");
        if (spaceIndex != -1) {
            Long userId = Long.valueOf(userMessage.substring(spaceIndex + 1));
            sendResponse(id.toString(), blockUserCommand.blockUser(userId));
        } else {
            sendResponse(id.toString(), getTemplate("error_id"));
        }
    }

    /**
     * Обработка команды /unblock_user
     *
     * @param userMessage полученное сообщение
     * @param id          id администратора
     */
    private void processingUnblockUserId(String userMessage, Long id) {
        int spaceIndex = userMessage.indexOf(" ");
        if (spaceIndex != -1) {
            Long userId = Long.valueOf(userMessage.substring(spaceIndex + 1));
            sendResponse(id.toString(), unblockUserCommand.unblockUser(userId));
        } else {
            sendResponse(id.toString(), getTemplate("error_id"));
        }
    }
}
