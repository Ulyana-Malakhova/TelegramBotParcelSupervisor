package org.example.Command;

import org.example.Dto.PackageDto;
import org.example.Service.PackageServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PackageCommand {
    private final PackageServiceImpl packageService;
    @Autowired
    public PackageCommand(PackageServiceImpl packageService) {
        this.packageService = packageService;
    }

    public void addNameTrackNumber(){

    }
    public void deleteNameTrackNumber(Long userId, String name) throws Exception {
        packageService.delete(userId, name);
    }
    public String getNamesTrackNumbers(Long userId){
        List<PackageDto> packageDtos = packageService.findByUser(userId);
        StringBuilder answer = new StringBuilder();
        if (packageDtos.isEmpty()) answer.append("Нет активных посылок");
        else for (PackageDto p: packageDtos){
            answer.append("Трек-номер: ").append(p.getTrackNumber()).append("\n");
            if (p.getNamePackage()!=null) answer.append("Имя: ").append(p.getNamePackage()).append("\n");
            answer.append("Роль: ").append(p.getNameRole()).append(", статус отслеживания: ").append(p.getNameTrackingStatus()).append("\n\n");
        }
        return answer.toString();
    }
}
