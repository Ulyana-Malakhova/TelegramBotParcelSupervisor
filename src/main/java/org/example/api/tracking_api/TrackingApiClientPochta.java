package org.example.api.tracking_api;

import org.example.AppConstants;
import org.example.Dto.PackageDto;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
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

    @Override
    public void receivingDeliveryData(PackageDto packageDto) throws IOException, ParseException {
        JSONObject jsonResponse = getParcelTrackingJson(packageDto.getTrackNumber());   //выполняем http-запрос
        JSONArray foundArray = jsonResponse.getJSONArray("detailedTrackings");
        JSONObject foundObject = foundArray.getJSONObject(0);
        JSONObject shipmentObject = foundObject.getJSONObject("trackingItem").optJSONObject("shipmentTripInfo");
        if (shipmentObject!=null) {
            JSONObject object;
            if (packageDto.getDepartureDate()==null) {
                object = shipmentObject.getJSONObject("acceptance");
                if (object != null) {
                    String departureDate = object.optString("date");
                    if (!departureDate.isEmpty()) packageDto.setDepartureDate(format.parse(departureDate));
                }
            }
            if (packageDto.getReceiptDate()==null) {
                object = shipmentObject.getJSONObject("arrived");
                if (object != null) {
                    String receiptDate = object.optString("date");
                    if (!receiptDate.isEmpty()) packageDto.setReceiptDate(format.parse(receiptDate));
                }
            }
        }
        //определение состояние по значению типа последней операции
        String typeOper = foundObject.getJSONObject("trackingItem").optString("lastOperationType");
        if (Integer.parseInt(typeOper)==2) packageDto.setNameTrackingStatus(AppConstants.DELIVERED);
        if (Integer.parseInt(typeOper)==3 || Integer.parseInt(typeOper)==5)
            packageDto.setNameTrackingStatus(AppConstants.CANCELED);
    }

    @Override
    public boolean isNumberPostalService(String number) {
        return false;
    }
}
