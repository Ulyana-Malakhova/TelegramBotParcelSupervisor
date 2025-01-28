package org.example.command;

import org.example.api.PackageLocation;
import org.example.api.tracking_api.TrackingApiClientBoxberry;
import org.example.api.tracking_api.TrackingApiClientCSE;
import org.example.api.tracking_api.TrackingApiClientDPD;

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
        answer = STR."Служба доставки: \{service.toString()}\{'\n'}";
        PackageLocation location = null;
        switch (service){   //вызываем метод получения местоположения в соответствии с сервисом
            case RUSSIAN_POST -> {}// TODO почта россии
            case DPD -> {
                location = apiDPD.getTrack(trackingNumber);
            }
            case CSE ->{
                location = apiCSE.getTrack(trackingNumber);
            }
            case BOXBERRY -> {
                location = apiBoxberry.getTrack(trackingNumber);
            }
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
        answer = STR."Служба доставки: \{service.toString()}\{'\n'}";
        PackageLocation[] locations = null;
        switch (service){   //вызываем метод получения истории в соответствии с сервисом
            case RUSSIAN_POST -> {}// TODO почта россии
            case DPD -> {
                locations = apiDPD.getHistoryTrack(trackingNumber);
            }
            case CSE ->{
                locations = apiCSE.getHistoryTrack(trackingNumber);
            }
            case BOXBERRY -> {
                locations = apiBoxberry.getHistoryTrack(trackingNumber);
            }
        }
        if (locations!=null) {
            for (PackageLocation p: locations)
            answer = STR."\{answer}\{p.toString()}\n"; //формируем окончательно сообщение
        }
        else answer = answer+errorNotFoundMessage;  //если данные о истории не получены - сообщение об ошибке
        return answer;
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
        else if (trackingNumber.matches("^\\d{3}-\\d{9}$")) return PostalService.CSE;
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
