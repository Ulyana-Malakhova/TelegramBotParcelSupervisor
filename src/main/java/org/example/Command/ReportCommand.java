package org.example.Command;

import org.example.Service.PackageService;
import org.example.TelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class ReportCommand {
    private final PackageService packageService;

    @Autowired
    public ReportCommand(PackageService packageService) {
        this.packageService = packageService;
    }

    public ByteArrayOutputStream execute(String period) throws IOException {
        return packageService.exportPackageToExcel(period);
    }

}
