package org.example.api.tracking_api;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Класс обработки http-ответов
 */
public class CustomHttpClientResponseHandler implements HttpClientResponseHandler<JSONObject> {
    @Override
    public JSONObject handleResponse(ClassicHttpResponse response) throws IOException, ParseException {
        int statusCode = response.getCode();    //проверка статуса ответа
        if (statusCode != 200) {
            throw new IOException("HTTP error - "+statusCode);
        }
        String responseString = EntityUtils.toString(response.getEntity()); //получение строки из http-entity
        return new JSONObject(responseString);  //преобразование в json
    }
}