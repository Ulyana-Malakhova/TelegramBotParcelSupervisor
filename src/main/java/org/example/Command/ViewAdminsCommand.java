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
public class ViewAdminsCommand {
    private final UserServiceImpl userService;

    @Autowired
    public ViewAdminsCommand(UserServiceImpl userService) {
        this.userService = userService;
    }

    /**
     * Получение списка администраторов
     * @return dto-список администраторов
     * @throws Exception не найдена сущность статуса администратора
     */
    public List<UserDto> getAdmins() throws Exception {
        return userService.findByStatus(AppConstants.STATUS_ADMIN);
    }

    public ByteArrayOutputStream execute() throws Exception {
        return userService.exportAdminsToExcel();
    }
}
