package org.example.Command;

import org.example.AppConstants;
import org.example.Dto.UserDto;
import org.example.Service.EmailService;
import org.example.Service.PasswordUtil;
import org.example.Service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserDataCommand {
    /**
     * Сервис для объектов пользователей
     */
    private final UserServiceImpl userService;
    private final EmailService emailService;
    @Autowired
    public UserDataCommand(UserServiceImpl userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    /**
     * Обновление почты администратора
     * @param id id пользователя
     * @param email новая электронная почта
     * @return true - почта успешно обновлена, иначе - false
     * @throws Exception не найден статус пользователя
     */
    public boolean updateEmail(Long id, String email) throws Exception {
        UserDto userDto = userService.get(id);
        if (userDto == null) return false;
        userDto.setEmail(email);
        userService.save(userDto);
        return true;
    }

    /**
     * Отправка администратору нового пароля
     * @param id id пользователя
     * @return true - пароль изменен, иначе - false
     * @throws Exception не найден статус пользователя
     */
    public boolean updatePassword(Long id) throws Exception {
        UserDto userDto = userService.get(id);
        if (userDto == null) return false;
        String password = emailService.sendPassword(userDto.getEmail());
        userDto.setPassword(PasswordUtil.hashPassword(password));
        userService.save(userDto);
        return true;
    }
    public UserDto getAdminDto(Long id){
        UserDto userDto = userService.get(id);
        if (userDto== null || !userDto.getNameStatus().equals(AppConstants.STATUS_ADMIN)) return null;
        else return userDto;
    }
}
