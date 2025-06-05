package org.example.Service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.AppConstants;
import org.example.Dto.MessageDto;
import org.example.Dto.UserDto;
import org.example.Entity.Message;
import org.example.Entity.User;
import org.example.Repository.MessageRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements ServiceInterface<MessageDto, Message> {
    private final ModelMapper modelMapper;
    private final MessageRepository messageRepository;
    private final UserServiceImpl userService;

    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository,UserServiceImpl userService) {
        this.messageRepository = messageRepository;
        this.modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        this.userService=userService;
    }

    @Override
    public void save(MessageDto messageDto){
        Message message = modelMapper.map(messageDto,Message.class);
        User user = userService.findById(messageDto.getIdUser());
        if (user!=null) {
            message.setUser(user);
            messageRepository.save(message);
        }
    }

    @Override
    public MessageDto get(Long id) {
        MessageDto messageDto = null;
        Optional<Message> message = messageRepository.findById(id);
        if(message.isPresent()){
            messageDto = new MessageDto(message.get().getId(), message.get().getText(), message.get().getDate(), message.get().getUser().getId());
        }
        return messageDto;
    }

    /**
     * Получение dto-списка сообщений
     * @param messages список сущностей-сообщений
     * @return dto-список сообщений
     */
    @Override
    public List<MessageDto> toDto(List<Message> messages) {
        List<MessageDto> messageDtos = new ArrayList<>();
        for (Message message : messages) {
            messageDtos.add(new MessageDto(message.getId(), message.getText(), message.getDate(), message.getUser().getId()));
        }
        return messageDtos;
    }

    /**
     * Получение сообщений, в которых есть команды, связанные с трекингом
     * @param userId id пользователя
     * @return dto-список сообщений
     */
    public List<MessageDto> getMessageWithTrackingNumbers(Long userId){
        LocalDate twoMonthsAgo = LocalDate.now().minusMonths(2);
        Date dateToCompare = java.sql.Date.valueOf(twoMonthsAgo);
        List<Message> messages = messageRepository.findLatestMessagesWithTrackingNumbers(userId, dateToCompare);
        return toDto(messages);
    }

    /**
     * Получение последннего сообщения пользователя
     * @param userId id пользователя
     * @return dto-объект сообщения
     */
    public MessageDto getLatest(Long userId){
        Optional<Message> messageOptional = messageRepository.findLatestMessageByUserId(userId);
        if (messageOptional.isPresent()) {
            Message message = messageOptional.get();
            MessageDto messageDto = modelMapper.map(message, MessageDto.class);
            messageDto.setIdUser(message.getUser().getId());
            return messageDto;
        }
        else
            return null;

    }

    /**
     * Создание и заполнение эксель файла со статистикой
     *
     * @return поток с эксель файлом
     * @throws Exception при работе с workbook, если произойдет ошибка при его создании, при записи в лист, при записи данных в поток или при закрытии workbook
     */
    public ByteArrayOutputStream statsInExcel() throws Exception {
        List<UserDto> activeUsers = userService.findByStatus(AppConstants.STATUS_USER);
        List<UserDto> blockedUsers = userService.findByStatus(AppConstants.STATUS_BLOCKED);
        List<UserDto> admins = userService.findByStatus(AppConstants.STATUS_ADMIN);

        //Пользователи с максимальным количеством запросов
        List<Object[]> results = messageRepository.findTopUsers(PageRequest.of(0, 3));
        List<Long> userIds = new ArrayList<>();
        for (Object[] row : results) {
            userIds.add((Long) row[0]);
        }

        // Пользователи с подозрением на спам
        List<Long> userSpamIds = new ArrayList<>();
        for (UserDto user: activeUsers) {
            long countMessageNotSlash = messageRepository.countMessagesNotStartingWithSlash(user.getId());
            long countMessageSlash = messageRepository.countMessagesStartingWithSlash(user.getId());
            if (countMessageNotSlash >= countMessageSlash){
                userSpamIds.add(user.getId());
            }
        }

        // Пиковые часы использования
        List<Timestamp> timestamps= messageRepository.findAllDate();
        List<LocalDateTime> dates = timestamps.stream()
                .map(Timestamp::toLocalDateTime)
                .collect(Collectors.toList());

        Map<String, Long> timeFreqMap = dates.stream()
                .collect(Collectors.groupingBy(
                        date -> String.format("%02d:%02d", date.getHour(), date.getMinute()),
                        Collectors.counting()
                ));

        long maxCount = timeFreqMap.values().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0);

        List<String> peakTimes = timeFreqMap.entrySet().stream()
                .filter(entry -> entry.getValue() == maxCount)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Statistics");
        CellStyle cellStyle = workbook.createCellStyle();
        Row headerRow = sheet.createRow(0);

        headerRow.createCell(0).setCellValue("Количество пользователей");
        sheet.setColumnWidth(0, 256 * 25);
        headerRow.createCell(1).setCellValue("Количество активных пользователей");
        sheet.setColumnWidth(1, 256 * 35);
        headerRow.createCell(2).setCellValue("Количество заблокированных пользователей");
        sheet.setColumnWidth(2, 256 * 40);
        headerRow.createCell(3).setCellValue("Количество администраторов");
        sheet.setColumnWidth(3, 256 * 35);
        headerRow.createCell(4).setCellValue("Пользователи с максимальным числом запросов");
        sheet.setColumnWidth(4, 256 * 45);
        headerRow.createCell(5).setCellValue("Подозрение на спам");
        sheet.setColumnWidth(5, 256 * 25);
        headerRow.createCell(6).setCellValue("Пиковые часы использования бота");
        sheet.setColumnWidth(6, 256 * 35);

        int countOfUsers = activeUsers.size()+ blockedUsers.size()+admins.size();
        int countOfActiveUsers = activeUsers.size();
        int countOfBlockedUsers = blockedUsers.size();
        int countOfAdmins = admins.size();

        int rowNum = 1;
        Row row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(countOfUsers);
        row.createCell(1).setCellValue(countOfActiveUsers);
        row.createCell(2).setCellValue(countOfBlockedUsers);
        row.createCell(3).setCellValue(countOfAdmins);
        Cell idCell1;

        int maxCountRow = userIds.size();
        if (userSpamIds.size() > maxCountRow){
            maxCountRow = userSpamIds.size();
        }
        if (peakTimes.size() > maxCountRow){
            maxCountRow = peakTimes.size();
        }

        for (int i=0 ;i < maxCountRow; i++){
            if (userIds.size() > i){
                idCell1 = row.createCell(4);
                idCell1.setCellValue(userIds.get(i));
                idCell1.setCellStyle(cellStyle);
            }
            if (userSpamIds.size() > i){
                idCell1 = row.createCell(5);
                idCell1.setCellValue(userSpamIds.get(i));
                idCell1.setCellStyle(cellStyle);
            }
            if (peakTimes.size() > i){
                idCell1 = row.createCell(6);
                idCell1.setCellValue(peakTimes.get(i));
                idCell1.setCellStyle(cellStyle);
            }
            row = sheet.createRow(rowNum++);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream;
    }
}
