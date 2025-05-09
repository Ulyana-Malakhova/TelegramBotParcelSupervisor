package org.example.Service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.AppConstants;
import org.example.Entity.Status;
import org.example.Entity.User;
import org.example.Dto.UserDto;
import org.example.Repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements ServiceInterface<UserDto, User> {
    private final UserRepository userRepository;
    private final StatusServiceImpl statusService;
    private final ModelMapper modelMapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, StatusServiceImpl statusService) {
        this.userRepository = userRepository;
        this.statusService = statusService;
        this.modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    /**
     * Получение dto-списка пользователей
     * @param users список сущностей пользователей
     * @return dto-список пользователей
     */
    @Override
    public List<UserDto> toDto(List<User> users) {
        List<UserDto> userDtos = new ArrayList<>();
        for (User user : users) {
            userDtos.add(new UserDto(user.getId(), user.getName(), user.getSurname(), user.getUsername(), user.getPhoneNumber(), user.getStatus().getStatusName(), user.getEmail(), user.getPassword()));
        }
        return userDtos;
    }

    @Override
    public void save(UserDto userDto) throws Exception {
        User userEntity = modelMapper.map(userDto, User.class);
        Status status = getStatus(userDto.getNameStatus());
        if (status != null) {
            userEntity.setStatus(status);
            userRepository.save(userEntity);
        }
    }

    @Override
    public UserDto get(Long id) {
        UserDto userDto = null;
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) userDto = new UserDto(user.get().getId(), user.get().getName(), user.get().getSurname(),
                user.get().getUsername(), user.get().getPhoneNumber(), user.get().getStatus().getStatusName(),
                user.get().getEmail(), user.get().getPassword());
        return userDto;
    }

    /**
     * Проверка, существует ли в БД пользователь с данным id чата
     *
     * @param id id чата
     * @return true - пользователь существует, иначе false
     */
    public boolean isUserExist(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.isPresent();
    }

    /**
     * Проверка, есть ли в БД зарегистрированные администраторы
     *
     * @return true - администратор есть, иначе - false
     */
    public boolean isAdministratorRegistered() throws Exception {
        Status status = getStatus(AppConstants.STATUS_ADMIN);
        List<User> admins = userRepository.findByStatus(status);
        return !admins.isEmpty();
    }

    /**
     * Получение статуса по названию
     *
     * @param status название статуса
     * @return сущность статуса
     * @throws Exception не найдена сущность статуса
     */
    public Status getStatus(String status) throws Exception {
        Status statusEntity = statusService.findByName(status);
        if (statusEntity == null) throw new Exception("Не удалось получить статус " + status);
        return statusEntity;
    }

    /**
     * Получение пользователя по id
     *
     * @param id id пользователя
     * @return сущность пользователя
     */
    public User findById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElse(null);
    }

    /**
     * Получение списка пользователей по статусу
     * @param statusString название статуса
     * @return dto-список пользователей
     * @throws Exception не найден статус
     */
    public List<UserDto> findByStatus(String statusString) throws Exception {
        Status status = getStatus(statusString);
        List<User> users = userRepository.findByStatus(status);
        return toDto(users);
    }

    /**
     * Создание и заполнение эксель файла с активными пользователями
     *
     * @return поток с эксель файлом
     * @throws Exception при работе с workbook, если произойдет ошибка при его создании, при записи в лист, при записи данных в поток или при закрытии workbook
     */
    public ByteArrayOutputStream exportActiveUsersToExcel() throws Exception {
        List<UserDto> users = findByStatus(AppConstants.STATUS_USER);

        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("ActiveUser");
        CellStyle cellStyle = workbook.createCellStyle();
        // Устанавливаем формат числа
        cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0"));
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("id");
        sheet.setColumnWidth(0, 256 * 15);
        headerRow.createCell(1).setCellValue("name");
        sheet.setColumnWidth(1, 256 * 15);
        headerRow.createCell(2).setCellValue("surname");
        sheet.setColumnWidth(2, 256 * 15);
        headerRow.createCell(3).setCellValue("username");
        sheet.setColumnWidth(3, 256 * 15);
        int rowNum = 1;
        for (UserDto userDto : users) {
            Row row = sheet.createRow(rowNum++);
            Cell idCell = row.createCell(0);
            idCell.setCellValue(userDto.getId());
            idCell.setCellStyle(cellStyle);
            row.createCell(1).setCellValue(userDto.getName());
            row.createCell(2).setCellValue(userDto.getSurname());
            row.createCell(3).setCellValue(userDto.getUsername());
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream;
    }

    /**
     * Создание и заполнение эксель файла с заблокированными пользователями
     *
     * @return поток с эксель файлом
     * @throws Exception при работе с workbook, если произойдет ошибка при его создании, при записи в лист, при записи данных в поток или при закрытии workbook
     */
    public ByteArrayOutputStream exportBlockedUsersToExcel() throws Exception {
        List<UserDto> blockedUsers = findByStatus(AppConstants.STATUS_BLOCKED);

        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("BlockedUser");
        CellStyle cellStyle = workbook.createCellStyle();
        // Устанавливаем формат числа
        cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0"));
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("id");
        sheet.setColumnWidth(0, 256 * 15);
        headerRow.createCell(1).setCellValue("name");
        sheet.setColumnWidth(1, 256 * 15);
        headerRow.createCell(2).setCellValue("surname");
        sheet.setColumnWidth(2, 256 * 15);
        headerRow.createCell(3).setCellValue("username");
        sheet.setColumnWidth(3, 256 * 15);
        int rowNum = 1;
        for (UserDto userDto : blockedUsers) {
            Row row = sheet.createRow(rowNum++);
            Cell idCell = row.createCell(0);
            idCell.setCellValue(userDto.getId());
            idCell.setCellStyle(cellStyle);
            row.createCell(1).setCellValue(userDto.getName());
            row.createCell(2).setCellValue(userDto.getSurname());
            row.createCell(3).setCellValue(userDto.getUsername());
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream;
    }

    /**
     * Создание и заполнение эксель файла с администраторами
     *
     * @return поток с эксель файлом
     * @throws Exception при работе с workbook, если произойдет ошибка при его создании, при записи в лист, при записи данных в поток или при закрытии workbook
     */
    public ByteArrayOutputStream exportAdminsToExcel() throws Exception {
        List<UserDto> admins = findByStatus(AppConstants.STATUS_ADMIN);

        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Administrators");
        CellStyle cellStyle = workbook.createCellStyle();
        // Устанавливаем формат числа
        cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0"));
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("id");
        sheet.setColumnWidth(0, 256 * 15);
        headerRow.createCell(1).setCellValue("name");
        sheet.setColumnWidth(1, 256 * 15);
        headerRow.createCell(2).setCellValue("surname");
        sheet.setColumnWidth(2, 256 * 15);
        headerRow.createCell(3).setCellValue("username");
        sheet.setColumnWidth(3, 256 * 15);
        headerRow.createCell(4).setCellValue("email");
        sheet.setColumnWidth(4, 256 * 20);

        int rowNum = 1;
        for (UserDto userDto : admins) {
            Row row = sheet.createRow(rowNum++);
            Cell idCell = row.createCell(0);
            idCell.setCellValue(userDto.getId());
            idCell.setCellStyle(cellStyle);
            row.createCell(1).setCellValue(userDto.getName());
            row.createCell(2).setCellValue(userDto.getSurname());
            row.createCell(3).setCellValue(userDto.getUsername());
            row.createCell(4).setCellValue(userDto.getEmail());
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream;
    }

    public void updateStatusById(Status status, Long id) {
        Optional<User> user = userRepository.findById(id);
        User userEntity = user.get();
        userEntity.setStatus(status);
        userRepository.save(userEntity);
    }
}
