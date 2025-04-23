package org.example.api.tracking_api;

import org.example.Dto.PackageDto;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;

/**
 * Базовый почтовый сервис
 */
public class TrackingApiClientBasic extends TrackingApiClient{
    @Override
    protected JSONArray getStatuses(JSONObject jsonResponse) {
        return null;
    }

    @Override
    public void receivingDeliveryData(PackageDto packageDto) throws IOException, ParseException {

    }

    @Override
    public boolean isNumberPostalService(String number) {
        return false;
    }
}
