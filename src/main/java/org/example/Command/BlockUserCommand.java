package org.example.Command;

import org.example.AppConstants;
import org.example.Entity.Status;
import org.example.Entity.User;
import org.example.Service.StatusServiceImpl;
import org.example.Service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BlockUserCommand {
    private final UserServiceImpl userService;
    private final StatusServiceImpl statusService;

    @Autowired
    public BlockUserCommand(UserServiceImpl userService, StatusServiceImpl statusService) {
        this.userService = userService;
        this.statusService = statusService;
    }

    public String blockUser(Long id) {
        Status statusEntity = statusService.findByName(AppConstants.STATUS_BLOCKED);
        User user = userService.findById(id);
        if (user == null) {
            return "Пользователь не найден";
        } else if (user.getStatus().getStatusName().equals(AppConstants.STATUS_BLOCKED)) {
            return "Пользователь уже заблокирован";
        } else {
            userService.updateStatusById(statusEntity, id);
            return "Пользователь успешно заблокирован";
        }
    }
}
