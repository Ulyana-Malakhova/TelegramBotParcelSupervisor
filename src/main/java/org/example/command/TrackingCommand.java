package org.example.Command;

import org.example.Dto.PackageDto;
import org.example.api.PackageLocation;
import org.example.api.tracking_api.TrackingApiClientBoxberry;
import org.example.api.tracking_api.TrackingApiClientCSE;
import org.example.api.tracking_api.TrackingApiClientDPD;
import org.example.api.tracking_api.TrackingApiClientPochta;

import java.io.IOException;
import java.text.ParseException;

/**
 * Класс обработки команд трекинга
 */
public class TrackingCommand {
    /**
     * Объект для использования api Boxberry
     */
    private final TrackingApiClientBoxberry apiBoxberry = new TrackingApiClientBoxberry();
    /**
     * Объект для использования api КСЭ
     */
    private final TrackingApiClientCSE apiCSE = new TrackingApiClientCSE();
    /**
     * Объект для использования api DPD
     */
    private final TrackingApiClientDPD apiDPD = new TrackingApiClientDPD();
    /**
     * Объект для использования api Почты России
     */
    private final TrackingApiClientPochta apiPochta = new TrackingApiClientPochta();
    /**
     * Сообщение при получении трек-номера, формат которого не совпадает с существующими
     */
    private final String errorMessage = "Неправильный формат номера. Проверьте правильность введенного трек-номера";
    /**
     * Сообщение для ситуации, когда о трек-номере не найдена информация в сервисе
     */
    private final String errorNotFoundMessage = "Данные по трек-номеру не найдены";

    /**
     * Метод для получения текущего положения посылки по команде /track
     * @param trackingNumber трек-номер
     * @return сообщение с информацией о местоположении
     */
    public String getTrackingMessage(String trackingNumber) {
        PostalService service = serviceDefinition(trackingNumber);  //определяем, к какому сервису относится трек-номер
        String answer;
        if (service==null){ //если сервис не определен - возвращаем сообщение об ошибке
            answer = errorMessage;
            return answer;
        }
        answer = "Служба доставки: "+service.toString()+'\n';
        PackageLocation location = null;
        switch (service){   //вызываем метод получения местоположения в соответствии с сервисом
            case RUSSIAN_POST -> location=apiPochta.getTrack(trackingNumber);
            case DPD -> location = apiDPD.getTrack(trackingNumber);
            case CSE -> location = apiCSE.getTrack(trackingNumber);
            case BOXBERRY -> location = apiBoxberry.getTrack(trackingNumber);
        }
        if (location!=null) answer = answer + location; //формируем окончательно сообщение
        else answer = answer+errorNotFoundMessage;  //если данные о местоположении не получены - сообщение об ошибке
        return answer;
    }

    /**
     * Метод для получения полной истории перемещения посылки
     * @param trackingNumber трек-номер
     * @return сообщение с информацией о передвижениях посылки
     */
    public String getHistoryMessage(String trackingNumber){
        PostalService service = serviceDefinition(trackingNumber);  //определяем, к какому сервису относится трек-номер
        String answer;
        if (service==null){ //если сервис не определен - возвращаем сообщение об ошибке
            answer = errorMessage;
            return answer;
        }
        answer = "Служба доставки: "+service.toString()+'\n';
        PackageLocation[] locations = null;
        switch (service){   //вызываем метод получения истории в соответствии с сервисом
            case RUSSIAN_POST -> locations = apiPochta.getHistoryTrack(trackingNumber);
            case DPD -> locations = apiDPD.getHistoryTrack(trackingNumber);
            case CSE ->locations = apiCSE.getHistoryTrack(trackingNumber);
            case BOXBERRY -> locations = apiBoxberry.getHistoryTrack(trackingNumber);
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
        PostalService service = serviceDefinition(packageDto.getTrackNumber());  //определяем, к какому сервису относится трек-номер
        switch (service){   //вызываем метод в соответствии с сервисом
            case RUSSIAN_POST -> apiPochta.receivingDeliveryData(packageDto);
            case DPD -> apiDPD.receivingDeliveryData(packageDto);
            case CSE -> apiCSE.receivingDeliveryData(packageDto);
            case BOXBERRY -> apiBoxberry.receivingDeliveryData(packageDto);
        }
    }

    /**
     * Метод для определения принадлежности трек номера почтовому сервису
     * @param trackingNumber трек-номер
     * @return элемент перечисления почтового сервиса
     */
    public PostalService serviceDefinition(String trackingNumber){
        if ((trackingNumber.length()==14 && isOnlyNumbers(trackingNumber)) ||
                trackingNumber.matches("^[a-zA-Z]{2}\\d{9}[a-zA-Z]{2}$")) return PostalService.RUSSIAN_POST;
        else if ((trackingNumber.length()==13 && isOnlyNumbers(trackingNumber)) ||
                trackingNumber.matches("^[a-zA-Z]{3}\\d{9}$")) return PostalService.BOXBERRY;
        else if (trackingNumber.matches("^[a-zA-Z]{2}\\d{9}$")) return PostalService.DPD;
        else if (trackingNumber.matches("^\\d{3}-\\d{9}$") ||
                (isOnlyNumbers(trackingNumber) && trackingNumber.length()==11)
                || trackingNumber.matches("^[0-9]{3}-[A-Z][0-9]{6}-[0-9]{8}$")) return PostalService.CSE;
        else return null;
    }

    /**
     * Метод проверки, состоит ли трек-номер только из цифр
     * @param trackingNumber трек-номер
     * @return true - в номере только цифры, иначе - false
     */
    private boolean isOnlyNumbers(String trackingNumber){
        return trackingNumber.matches("^\\d+$");
    }
}
