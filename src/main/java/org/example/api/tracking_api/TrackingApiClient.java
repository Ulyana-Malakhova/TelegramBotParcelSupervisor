package org.example.api.tracking_api;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;
import org.example.api.PackageLocation;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Абстрактный класс для получения данных с api-запросов почтовых служб
 */
public abstract class TrackingApiClient {
    /**
     * URL, на который отправится запрос
     */
    protected String url;
    /**
     * Формат считывания даты
     */
    protected SimpleDateFormat format;
    /**
     * Название поля сообщения/статуса
     */
    protected String fieldMessage;
    /**
     * Название поля даты
     */
    protected String fieldDate;
    /**
     * Название поля местоположения
     */
    protected String fieldLocation;

    /**
     * Метод для отправки get-запроса сервисам и получения ответа
     * @param numberTrack строка-трек номер
     * @return json-ответ
     * @throws IOException статус ответа не OK, ошибка сети или проблемы с подключением
     * @throws org.apache.hc.core5.http.ParseException ошибка парсинга HTTP-ответа
     */
    public JSONObject getParcelTrackingJson(String numberTrack) throws IOException, org.apache.hc.core5.http.ParseException {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(60))
                .setResponseTimeout(Timeout.ofSeconds(40))
                .build();
        // Создаем HttpClient с заданными настройками
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
        HttpGet request = new HttpGet(url + numberTrack);
        CloseableHttpResponse response = httpClient.execute(request);
        int statusCode = response.getCode();
        if (statusCode != 200) {
            throw new IOException(STR."HTTP error - \{statusCode}");
        }
        String jsonString = EntityUtils.toString(response.getEntity());
        JSONObject jsonResponse = new JSONObject(jsonString);
        httpClient.close();
        response.close();
        return jsonResponse;
    }

    /**
     * Получение текущего статуса местоположения посылки
     * @param numberTrack строка-трек номер
     * @return объект PackageLocation с данными о местоположении
     */
    protected PackageLocation getTrack(String numberTrack){
        PackageLocation packageLocation = new PackageLocation();
        try {
            JSONObject jsonResponse = getParcelTrackingJson(numberTrack);
            JSONArray statusesArray = getStatuses(jsonResponse);
            JSONObject statusObject = statusesArray.getJSONObject(statusesArray.length() - 1);
            parser(packageLocation, statusObject);
            return packageLocation;
        } catch (IOException | org.apache.hc.core5.http.ParseException ex) {
            System.out.println(STR."Ошибка отправки http-запроса: \{ex.getMessage()}");
        } catch (JSONException e) {
            System.out.println(STR."Ошибка парсинга JSON: \{e.getMessage()}");
        }
        return packageLocation;
    }

    /**
     * Получение всей истории передвижений посылки
     * @param numberTrack строка-трек номер
     * @return массив объектов PackageLocation с данными о местоположении
     */
    protected PackageLocation[] getHistoryTrack(String numberTrack){
        try {
            JSONObject jsonResponse = getParcelTrackingJson(numberTrack);
            JSONArray statusesArray = getStatuses(jsonResponse);
            PackageLocation[] packageLocations = new PackageLocation[statusesArray.length()];
            for (int j = 0; j < statusesArray.length(); j++) {
                JSONObject statusObject = statusesArray.getJSONObject(j);
                packageLocations[j] = new PackageLocation();
                parser(packageLocations[j], statusObject);
            }
            return packageLocations;
        }catch (IOException| org.apache.hc.core5.http.ParseException ex){
            System.out.println(STR."Ошибка отправки http-запроса: \{ex.getMessage()}");
        }
        catch (JSONException e){
            System.out.println(STR."Ошибка парсинга JSON: \{e.getMessage()}");
        }
        return null;
    }

    /**
     * Метод для получения json-массива с данными о перемещениях посылки
     * @param jsonResponse json-объект ответа почтового api
     * @return json-массива с данными о перемещениях посылки
     */
    protected abstract JSONArray getStatuses(JSONObject jsonResponse);

    /**
     * Парсинг json для сохранения данных о местоположении
     * @param packageLocation объект для сохранения информации
     * @param jsonResponse объект json
     */
    public void parser(PackageLocation packageLocation, JSONObject jsonResponse){
        packageLocation.setMessage(jsonResponse.optString(fieldMessage));
        packageLocation.setLocation(jsonResponse.optString(fieldLocation));
        try {
            packageLocation.setDate(format.parse(jsonResponse.optString(fieldDate)));
        } catch (ParseException e) {
            System.out.println("Ошибка форматирования даты");
        }
    }
}
