package org.example.Command;

import org.example.Dto.UserDto;
import org.example.Service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AdminDataCommand {
    /**
     * Сервис для объектов пользователей
     */
    private final UserServiceImpl userService;
    @Autowired
    public AdminDataCommand(UserServiceImpl userService) {
        this.userService = userService;
    }
    public void updateEmail(Long id, String email) throws Exception {
        UserDto userDto = userService.get(id);
        userDto.setEmail(email);
        userService.save(userDto);
    }
}
