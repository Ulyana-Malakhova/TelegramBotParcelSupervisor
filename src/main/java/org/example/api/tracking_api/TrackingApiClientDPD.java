package org.example.api.tracking_api;

import org.example.AppConstants;
import org.example.Dto.PackageDto;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Класс для получения данных с api-запросов DPD
 */
public class TrackingApiClientDPD extends TrackingApiClient {
    {
        url="https://dpd-site-tracing-backend-api-prod.dpd.ru/api/v3/order?orderNumber=";
        format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        fieldMessage = "state_name";
        fieldLocation = "state_city";
        fieldDate = "state_moment";
    }
    @Override
    protected JSONArray getStatuses(JSONObject jsonResponse) {
        return jsonResponse.getJSONArray("state");
    }

    @Override
    public void receivingDeliveryData(PackageDto packageDto) throws IOException, ParseException {
        JSONObject jsonResponse = getParcelTrackingJson(packageDto.getTrackNumber());   //выполняем http-запрос
        JSONArray statusesArray = getStatuses(jsonResponse);    //из json получаем список данных о местоположении
        JSONObject statusObject = statusesArray.getJSONObject(0);   //для получения даты отправки берем первый статус
        if (statusObject!=null && packageDto.getDepartureDate()==null && !statusObject.optString(fieldDate).isEmpty())
            packageDto.setDepartureDate(format.parse(statusObject.optString(fieldDate)));
        if (packageDto.getReceiptDate()==null && jsonResponse.getJSONObject("shipment")!=null) {
            statusObject = jsonResponse.getJSONObject("shipment");
            if (!statusObject.optString("delivery_date").isEmpty()) //дата получения из поля
                packageDto.setReceiptDate(format.parse(statusObject.optString("delivery_date")));
            String status = statusObject.optString("state_code");
            if (!status.isEmpty()) {
                if (status.equals("ДО"))
                    packageDto.setNameTrackingStatus(AppConstants.DELIVERED);
                if (status.equals("НВ"))
                    packageDto.setNameTrackingStatus(AppConstants.CANCELED);
            }
        }
        statusObject = statusesArray.getJSONObject(statusesArray.length()-1);
        String latestStatus = "";
        if (!statusObject.optString(fieldMessage).isEmpty()) latestStatus = statusObject.optString(fieldMessage);
        if (!statusObject.optString(fieldLocation).isEmpty()) latestStatus = latestStatus+" "+statusObject.optString(fieldLocation);
        if (packageDto.getLatestStatus()==null || !latestStatus.equals(packageDto.getLatestStatus())) packageDto.setLatestStatus(latestStatus);

    }
}

