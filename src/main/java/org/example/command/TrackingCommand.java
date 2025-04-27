package org.example.Command;

import org.example.Dto.PackageDto;
import org.example.api.PackageLocation;
import org.example.api.tracking_api.TrackingApiClient;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;

import java.io.IOException;
import java.lang.module.Configuration;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Класс обработки команд трекинга
 */
public class TrackingCommand {
    /**
     * Сообщение для ситуации, когда о трек-номере не найдена информация в сервисе
     */
    private final String errorNotFoundMessage = "Данные по трек-номеру не найдены";
    /**
     * Слой модулей
     */
    private final ModuleLayer layer;

    public TrackingCommand(){
        Path pluginsDir = Paths.get("src/main/resources/plugins");
        ModuleFinder pluginsFinder = ModuleFinder.of(pluginsDir);// поиск плагинов
        List<String> plugins = pluginsFinder    //получение списка имен плагинов
                .findAll()
                .stream()
                .map(ModuleReference::descriptor)
                .map(ModuleDescriptor::name)
                .collect(Collectors.toList());
        Configuration pluginsConfiguration = ModuleLayer    // Конфигурация, которая выполнит резолюцию указанных модулей (проверит корректность графа зависимостей)
                .boot()
                .configuration()
                .resolve(pluginsFinder, ModuleFinder.of(), plugins);
        layer = ModuleLayer //создание слоя модулей
                .boot()
                .defineModulesWithOneLoader(pluginsConfiguration, ClassLoader.getSystemClassLoader());
    }
    /**
     * Метод для получения текущего положения посылки по команде /track
     * @param trackingNumber трек-номер
     * @return сообщение с информацией о местоположении
     */
    public String getTrackingMessage(String trackingNumber) {
        String answer = "";
        PackageLocation location = null;
        List<TrackingApiClient> services = TrackingApiClient.getServices(layer);
        for (TrackingApiClient api : services) {
            if (api.isNumberPostalService(trackingNumber)) location = api.getTrack(trackingNumber);
        }
        //если данные о местоположении не получены - сообщение об ошибке
        answer = answer + Objects.requireNonNullElse(location, errorNotFoundMessage); //формируем окончательно сообщение
        return answer;
    }

    /**
     * Метод для получения полной истории перемещения посылки
     * @param trackingNumber трек-номер
     * @return сообщение с информацией о передвижениях посылки
     */
    public String getHistoryMessage(String trackingNumber){
        String answer = "";
        PackageLocation[] locations = null;
        List<TrackingApiClient> services = TrackingApiClient.getServices(layer);
        for (TrackingApiClient api : services) {
            if (api.isNumberPostalService(trackingNumber)) locations = api.getHistoryTrack(trackingNumber);
        }
        if (locations!=null) {
            for (PackageLocation p: locations)
                answer = answer+p.toString()+'\n'; //формируем окончательно сообщение
        }
        else answer = answer+errorNotFoundMessage;  //если данные о истории не получены - сообщение об ошибке
        return answer;
    }

    /**
     * Метод для обновления данных посылки об отправке/получении
     * @param packageDto dto объект посылки
     * @throws IOException статус ответа не OK, ошибка сети или проблемы с подключением
     * @throws ParseException ошибка в парсинге даты
     */
    public void updateParcelDetails(PackageDto packageDto) throws IOException, ParseException {
        List<TrackingApiClient> services = TrackingApiClient.getServices(layer);
        for (TrackingApiClient api : services) {
            if (api.isNumberPostalService(packageDto.getTrackNumber())) api.receivingDeliveryData(packageDto);
        }
    }

    /**
     * Проерка принадлежности трек-номера одному из почтовых сервисов
     * @param trackingNumber строка - трек-номер
     * @return true - номер относится к одному из сервисов, иначе - false
     */
    public boolean isNumberPostal(String trackingNumber){
        List<TrackingApiClient> services = TrackingApiClient.getServices(layer);
        for (TrackingApiClient api : services) {
            if (api.isNumberPostalService(trackingNumber)) return true;
        }
        return false;
    }

    /**
     * Метод проверки, состоит ли трек-номер только из цифр
     * @param trackingNumber трек-номер
     * @return true - в номере только цифры, иначе - false
     */
    public boolean isOnlyNumbers(String trackingNumber){
        return trackingNumber.matches("^\\d+$");
    }
}
