package org.example.Command;

import org.example.AppConstants;
import org.example.Dto.UserDto;
import org.example.Entity.User;
import org.example.Service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Component
public class ViewUsersCommand {
    private final UserServiceImpl userService;

    @Autowired
    public ViewUsersCommand(UserServiceImpl userService) {
        this.userService = userService;
    }

    /**
     * Получение списка обычных пользователей
     * @return dto-список пользователей
     * @throws Exception не найдена сущность статуса пользователя
     */
    public List<UserDto> getUsers() throws Exception {
        return userService.findByStatus(AppConstants.STATUS_USER);
    }

    public ByteArrayOutputStream execute() throws Exception {
        return userService.exportActiveUsersToExcel();
    }
}
