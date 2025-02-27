package org.example.api.tracking_api;

import org.example.AppConstants;
import org.example.Dto.PackageDto;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Класс для получения данных с api-запросов Boxberry
 */
public class TrackingApiClientBoxberry extends TrackingApiClient {

    @Override
    protected JSONArray getStatuses(JSONObject jsonResponse) {
        JSONArray parcelWithStatuses = jsonResponse.getJSONArray("parcel_with_statuses");
        return parcelWithStatuses.getJSONObject(0).getJSONArray("Statuses");
    }

    @Override
    public void receivingDeliveryData(PackageDto packageDto) throws IOException, ParseException {
        JSONObject jsonResponse = getParcelTrackingJson(packageDto.getTrackNumber());   //выполняем http-запрос
        JSONArray statusesArray = getStatuses(jsonResponse);    //из json получаем список данных о местоположении
        JSONObject statusObject = statusesArray.getJSONObject(0);   //для получения даты отправки берем первый статус
        if (statusObject!=null && !statusObject.optString(fieldDate).isEmpty() && packageDto.getDepartureDate()==null)
            packageDto.setDepartureDate(format.parse(statusObject.optString(fieldDate)));
        String status = jsonResponse.getJSONArray("parcel_with_statuses").getJSONObject(0)
                .optString("status_code");  //получаем текущий код статуса посылки
        if (!status.isEmpty() && (Integer.parseInt(status)==190 || Integer.parseInt(status)==150)
                && packageDto.getReceiptDate()==null) { //дату получения берем из последнего статуса
            statusObject = statusesArray.getJSONObject(statusesArray.length()-1);
            if (!statusObject.optString(fieldDate).isEmpty())
                packageDto.setReceiptDate(format.parse(statusObject.optString(fieldDate)));
            if (Integer.parseInt(status)==190) packageDto.setNameTrackingStatus(AppConstants.CANCELED);
            if (Integer.parseInt(status)==150) packageDto.setNameTrackingStatus(AppConstants.DELIVERED);
        }
    }

    public TrackingApiClientBoxberry(){
        this.url="https://boxberry.ru/api/v1/tracking/order/get?searchId=";
        this.format = new SimpleDateFormat("dd.MM.yyyy (HH:mm)");
        this.fieldMessage = "name";
        this.fieldLocation = "location";
        this.fieldDate = "date_time";
    }
}
