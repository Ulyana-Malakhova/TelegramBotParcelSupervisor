package org.example.Command;

import org.example.Service.MessageServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component
public class StatsCommand {
    private final MessageServiceImpl messageService;

    @Autowired
    public StatsCommand(MessageServiceImpl messageService) {
        this.messageService = messageService;
    }

    public ByteArrayOutputStream execute() throws Exception {
        return messageService.statsInExcel();
    }
}
