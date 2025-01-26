package org.example.api.tracking_api;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

/**
 * Класс для получения данных с api-запросов DPD
 */
public class TrackingApiClientDPD extends TrackingApiClient {
    @Override
    protected JSONArray getStatuses(JSONObject jsonResponse) {
        return jsonResponse.getJSONArray("state");
    }

    TrackingApiClientDPD(){
        this.url="https://dpd-site-tracing-backend-api-prod.dpd.ru/api/v3/order?orderNumber=";
        this.format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        this.fieldMessage = "state_name";
        this.fieldLocation = "state_city";
        this.fieldDate = "state_moment";
    }
}

