package org.example.Service;

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
public class UserServiceImpl implements ServiceInterface<UserDto> {
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

    private List<UserDto> toDto() {
        List<User> users = userRepository.findAll();
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
        if (status!=null) {
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
     * Создание и заполнение эксель файла с активными пользователями
     *
     * @return поток с эксель файлом
     * @throws Exception при работе с workbook, если произойдет ошибка при его создании, при записи в лист, при записи данных в поток или при закрытии workbook
     */
    public ByteArrayOutputStream exportActiveUsersToExcel() throws Exception {
        Status status = getStatus(AppConstants.STATUS_USER);
        List<User> users = userRepository.findByStatus(status);

        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("ActiveUser");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("id");
        headerRow.createCell(1).setCellValue("name");
        headerRow.createCell(2).setCellValue("surname");
        headerRow.createCell(3).setCellValue("username");

        int rowNum = 1;
        for (User userEntity : users) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(userEntity.getId());
            row.createCell(1).setCellValue(userEntity.getName());
            row.createCell(2).setCellValue(userEntity.getSurname());
            row.createCell(3).setCellValue(userEntity.getUsername());
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
        Status status = getStatus(AppConstants.STATUS_BLOCKED);
        List<User> blockedUsers = userRepository.findByStatus(status);

        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("BlockedUser");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("id");
        headerRow.createCell(1).setCellValue("name");
        headerRow.createCell(2).setCellValue("surname");
        headerRow.createCell(3).setCellValue("username");

        int rowNum = 1;
        for (User userEntity : blockedUsers) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(userEntity.getId());
            row.createCell(1).setCellValue(userEntity.getName());
            row.createCell(2).setCellValue(userEntity.getSurname());
            row.createCell(3).setCellValue(userEntity.getUsername());
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
        Status status = getStatus(AppConstants.STATUS_ADMIN);
        List<User> admins = userRepository.findByStatus(status);

        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Administrators");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("id");
        headerRow.createCell(1).setCellValue("name");
        headerRow.createCell(2).setCellValue("surname");
        headerRow.createCell(3).setCellValue("username");

        int rowNum = 1;
        for (User userEntity : admins) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(userEntity.getId());
            row.createCell(1).setCellValue(userEntity.getName());
            row.createCell(2).setCellValue(userEntity.getSurname());
            row.createCell(3).setCellValue(userEntity.getUsername());
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream;
    }
}
