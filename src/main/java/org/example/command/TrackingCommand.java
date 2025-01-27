package org.example.command;

import org.example.api.PackageLocation;
import org.example.api.tracking_api.TrackingApiClientBoxberry;
import org.example.api.tracking_api.TrackingApiClientCSE;
import org.example.api.tracking_api.TrackingApiClientDPD;

public class TrackingCommand {
    private final TrackingApiClientBoxberry apiBoxberry = new TrackingApiClientBoxberry();
    private final TrackingApiClientCSE apiCSE = new TrackingApiClientCSE();
    private final TrackingApiClientDPD apiDPD = new TrackingApiClientDPD();
    private final String errorMessage = "Неправильный формат номера. Проверьте правильность введенного трек-номера";
    private final String errorNotFoundMessage = "Данные по трек-номеру не найдены";
    public String getTrackingMessage(String trackingNumber) {
        PostalService service = serviceDefinition(trackingNumber);
        String answer;
        if (service==null){
            answer = errorMessage;
            return answer;
        }
        answer = STR."Служба доставки: \{service.toString()}\{'\n'}";
        PackageLocation location = null;
        switch (service){
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
        if (location!=null) answer = answer + location;
        else answer = answer+errorNotFoundMessage;
        return answer;
    }
    public String getHistoryMessage(String trackingNumber){
        PostalService service = serviceDefinition(trackingNumber);
        String answer;
        if (service==null){
            answer = errorMessage;
            return answer;
        }
        answer = STR."Служба доставки: \{service.toString()}\{'\n'}";
        PackageLocation[] locations = null;
        switch (service){
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
            answer = STR."\{answer}\{p.toString()}\n";
        }
        else answer = answer+errorNotFoundMessage;
        return answer;
    }
    public PostalService serviceDefinition(String trackingNumber){
        if ((trackingNumber.length()==14 && isOnlyNumbers(trackingNumber)) ||
                trackingNumber.matches("^[a-zA-Z]{2}\\d{9}[a-zA-Z]{2}$")) return PostalService.RUSSIAN_POST;
        else if ((trackingNumber.length()==13 && isOnlyNumbers(trackingNumber)) ||
                trackingNumber.matches("^[a-zA-Z]{3}\\d{9}$")) return PostalService.BOXBERRY;
        else if (trackingNumber.matches("^[a-zA-Z]{2}\\d{9}$")) return PostalService.DPD;
        else if (trackingNumber.matches("^\\d{3}-\\d{9}$")) return PostalService.CSE;
        else return null;
    }
    private boolean isOnlyNumbers(String trackingNumber){
        return trackingNumber.matches("^\\d+$");
    }
}
