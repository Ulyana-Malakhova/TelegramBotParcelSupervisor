package org.example.Command;

import org.example.Dto.PackageDto;
import org.example.Service.PackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Класс обработки команд взаимодействия с посылками
 */
@Component
public class PackageCommand {
    /**
     * Сервис для объектов посылок
     */
    private final PackageService packageService;
    @Autowired
    public PackageCommand(PackageService packageService) {
        this.packageService = packageService;
    }

    /**
     * Добавление имени посылке
     * @param packageDto dto-объект посылки
     * @throws Exception если имя уже используется текущим пользователем
     */
    public void addNameTrackNumber(PackageDto packageDto) throws Exception {
        packageService.addName(packageDto);
    }

    /**
     * Удаление имени посылки
     * @param userId id пользователя
     * @param name имя посылки
     * @throws Exception если нет посылки с переданным именем у текущего пользователя
     */
    public void deleteNameTrackNumber(Long userId, String name) throws Exception {
        packageService.delete(userId, name);
    }

    /**
     * Получение списка сохраненных посылок пользователя
     * @param userId id пользователя
     * @return строка с данными об отслеживаемых посылках и о посылках с именами
     */
    public String getSavedTrackNumbers(Long userId){
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

    /**
     * Поиск посылки по имени
     * @param userId id пользователя
     * @param name имя посылки
     * @return трек-номер посылки с данным именем, в случае ее отсутствия - null
     */
    public String findByName(Long userId, String name){
        return packageService.findByName(userId, name);
    }

    /**
     * Поиск посылки по трек-номеру
     * @param userId id пользователя
     * @param track трек-номер посылки
     * @return dto-объект посылки, в случае ее отсутствия - null
     */
    public PackageDto findByTrack(Long userId, String track){
        return packageService.findByTrack(userId, track);
    }
}
