package org.example.Command;

import org.example.AppConstants;
import org.example.Entity.Status;
import org.example.Entity.User;
import org.example.Service.StatusServiceImpl;
import org.example.Service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UnblockUserCommand {
    private final UserServiceImpl userService;
    private final StatusServiceImpl statusService;

    @Autowired
    public UnblockUserCommand(UserServiceImpl userService, StatusServiceImpl statusService) {
        this.userService = userService;
        this.statusService = statusService;
    }

    public String unblockUser(Long id) {
        Status statusEntity = statusService.findByName(AppConstants.STATUS_USER);
        User user = userService.findById(id);
        if (user == null) {
            return "Пользователь не найден";
        } else if (!user.getStatus().getStatusName().equals(AppConstants.STATUS_BLOCKED)) {
            return "Пользователь не заблокирован";
        } else {
            userService.updateStatusById(statusEntity, id);
            return "Пользователь успешно разблокирован";
        }
    }
}
