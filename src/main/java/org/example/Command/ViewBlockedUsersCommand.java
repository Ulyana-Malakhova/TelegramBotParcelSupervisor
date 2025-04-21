package org.example.Command;

import org.example.Service.UserServiceImpl;
import org.example.TelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class ViewBlockedUsersCommand {
    private final UserServiceImpl userService;

    @Autowired
    public ViewBlockedUsersCommand(UserServiceImpl userService) {
        this.userService = userService;
    }

    public ByteArrayOutputStream execute() throws Exception {
        return userService.exportBlockedUsersToExcel();
    }
}
