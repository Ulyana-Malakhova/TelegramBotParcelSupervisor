package org.example.api.tracking_api;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

/**
 * Класс для получения данных с api-запросов Boxberry
 */
public class TrackingApiClientBoxberry extends TrackingApiClient {

    @Override
    protected JSONArray getStatuses(JSONObject jsonResponse) {
        JSONArray parcelWithStatuses = jsonResponse.getJSONArray("parcel_with_statuses");
        return parcelWithStatuses.getJSONObject(0).getJSONArray("Statuses");
    }

    TrackingApiClientBoxberry(){
        this.url="https://boxberry.ru/api/v1/tracking/order/get?searchId=";
        this.format = new SimpleDateFormat("dd.MM.yyyy (HH:mm)");
        this.fieldMessage = "name";
        this.fieldLocation = "location";
        this.fieldDate = "date_time";
    }
}
