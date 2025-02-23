package org.example.api.tracking_api;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

/**
 * Класс для получения данных с api-запросов Почты России
 */
public class TrackingApiClientPochta extends TrackingApiClient {
    public TrackingApiClientPochta() {
        this.url = "https://www.pochta.ru/api/tracking/api/v1/trackings/by-barcodes?language=ru&track-numbers=";
        this.format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        this.fieldMessage = "commonStatus";
        this.fieldLocation = null;
        this.fieldDate = "lastOperationDateTime";
    }

    @Override
    protected JSONArray getStatuses(JSONObject jsonResponse) {
        JSONArray resultArray = new JSONArray();
        JSONArray foundArray = jsonResponse.getJSONArray("detailedTrackings");
        JSONObject foundObject = foundArray.getJSONObject(0);
        JSONObject historyObject = foundObject.getJSONObject("trackingItem");
        JSONObject orderStatusObject = new JSONObject();
        orderStatusObject.put("commonStatus", historyObject.getString("commonStatus"));
        orderStatusObject.put("lastOperationDateTime", historyObject.getString("lastOperationDateTime"));
        resultArray.put(orderStatusObject);
        return resultArray;
    }
}
