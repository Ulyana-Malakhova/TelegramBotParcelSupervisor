package org.example.api.tracking_api;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Класс для получения данных с api-запросов КСЭ
 */
public class TrackingApiClientCSE extends TrackingApiClient {

    @Override
    protected JSONArray getStatuses(JSONObject jsonResponse) {
        JSONArray foundArray = jsonResponse.getJSONArray("found");
        JSONObject foundObject = foundArray.getJSONObject(0);
        JSONObject historyObject = foundObject.getJSONObject("History");
        JSONArray mergedArray = null;
        if (historyObject.has("order_info")) {
            mergedArray = new JSONArray(historyObject.getJSONArray("order_info").toString());
        }
        if (historyObject.has("waybill_info")) {
            if (mergedArray==null) mergedArray = new JSONArray(historyObject.getJSONArray("waybill_info").toString());
            else {
                JSONArray waybillInfoArray = new JSONArray(historyObject.getJSONArray("waybill_info").toString());
                for (int i = 0; i < waybillInfoArray.length(); i++) {
                    mergedArray.put(waybillInfoArray.get(i));
                }
            }
        }
        sortJSONArrayByDate(mergedArray);
        return mergedArray;
    }

    /**
     * Метод для сортировки json-массива по дате
     * @param jsonArray сортируемый массив
     */
    public void sortJSONArrayByDate(JSONArray jsonArray) {
        JSONObject[] jsonObjects = new JSONObject[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            jsonObjects[i] = jsonArray.getJSONObject(i);
        }
        Arrays.sort(jsonObjects, (o1, o2) -> {
            try {
                Date date1 = format.parse(o1.getString(fieldDate));
                Date date2 = format.parse(o2.getString(fieldDate));
                return date1.compareTo(date2);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });
        jsonArray.clear();
        for (JSONObject jsonObject : jsonObjects) {
            jsonArray.put(jsonObject);
        }
    }

    TrackingApiClientCSE() {
        this.url = "https://lk.cse.ru/api/new-track/";
        this.format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        this.fieldMessage = "EventName";
        this.fieldLocation = "EventGeography";
        this.fieldDate = "EventDate";
    }
}
