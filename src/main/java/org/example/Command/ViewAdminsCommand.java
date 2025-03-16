package org.example.Command;

import org.example.Service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;


@Component
public class ViewAdminsCommand {
    private final UserServiceImpl userService;

    @Autowired
    public ViewAdminsCommand(UserServiceImpl userService) {
        this.userService = userService;
    }

    public ByteArrayOutputStream execute() throws Exception {
        return userService.exportAdminsToExcel();
    }
}
