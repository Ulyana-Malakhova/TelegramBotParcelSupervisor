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
 * Класс для получения данных с api-запросов DPD
 */
public class TrackingApiClientDPD extends TrackingApiClient {
    @Override
    protected JSONArray getStatuses(JSONObject jsonResponse) {
        return jsonResponse.getJSONArray("state");
    }

    @Override
    protected void receivingDeliveryData(PackageDto packageDto) throws IOException, ParseException {
        JSONObject jsonResponse = getParcelTrackingJson(packageDto.getTrackNumber());   //выполняем http-запрос
        JSONArray statusesArray = getStatuses(jsonResponse);    //из json получаем список данных о местоположении
        JSONObject statusObject = statusesArray.getJSONObject(0);
        if (statusObject!=null && packageDto.getDepartureDate()==null && statusObject.optString(fieldDate)!=null)
            packageDto.setDepartureDate(format.parse(statusObject.optString(fieldDate)));
        if (packageDto.getReceiptDate()==null && jsonResponse.getJSONObject("shipment")!=null) {
            statusObject = jsonResponse.getJSONObject("shipment");
            if (statusObject.optString("delivery_date")!=null)
                packageDto.setReceiptDate(format.parse(statusObject.optString("delivery_date")));
            String status = statusObject.optString("state_code");
            if (status!=null) {
                if (status.equals("ДО"))
                    packageDto.setNameTrackingStatus(AppConstants.DELIVERED);
                if (status.equals("НВ"))
                    packageDto.setNameTrackingStatus(AppConstants.CANCELED);
            }
        }
    }

    public TrackingApiClientDPD(){
        this.url="https://dpd-site-tracing-backend-api-prod.dpd.ru/api/v3/order?orderNumber=";
        this.format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        this.fieldMessage = "state_name";
        this.fieldLocation = "state_city";
        this.fieldDate = "state_moment";
    }
}

