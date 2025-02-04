package org.example.api.tracking_api;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.example.api.PackageLocation;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

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
        RequestConfig requestConfig = RequestConfig.custom()    //настройка параметров http-запроса
                .setResponseTimeout(Timeout.ofSeconds(40))
                .build();
        ConnectionConfig connectionConfig = ConnectionConfig.custom() //настройка параметров соединения
                .setConnectTimeout(60, TimeUnit.SECONDS)
                .build();
        BasicHttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager();   //объект поддержки одного активного соединения
        connectionManager.setConnectionConfig(connectionConfig);
        HttpGet request = new HttpGet(url + numberTrack);
        CloseableHttpClient httpClient = HttpClientBuilder.create() //объект выполнения http-запроса
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connectionManager)
                .build();
        JSONObject jsonResponse = httpClient.execute(request, new CustomHttpClientResponseHandler());
        httpClient.close();
        return jsonResponse;
    }
    /**
     * Получение текущего статуса местоположения посылки
     * @param numberTrack строка-трек номер
     * @return объект PackageLocation с данными о местоположении
     */
    public PackageLocation getTrack(String numberTrack){
        PackageLocation packageLocation = new PackageLocation();
        try {
            JSONObject jsonResponse = getParcelTrackingJson(numberTrack);   //выполняем http-запрос
            JSONArray statusesArray = getStatuses(jsonResponse);    //из json получаем список данных о местоположении
            //получаем данные о текущем положении и парсим
            JSONObject statusObject = statusesArray.getJSONObject(statusesArray.length() - 1);
            parser(packageLocation, statusObject);
            return packageLocation;
        } catch (IOException | org.apache.hc.core5.http.ParseException ex) {
            System.out.println(STR."Ошибка отправки http-запроса:\{ex.getMessage()}");
            return null;
        } catch (JSONException e) {
            System.out.println(STR."Ошибка парсинга JSON: \{e.getMessage()}");
            return null;
        }
    }

    /**
     * Получение всей истории передвижений посылки
     * @param numberTrack строка-трек номер
     * @return массив объектов PackageLocation с данными о местоположении
     */
    public PackageLocation[] getHistoryTrack(String numberTrack){
        try {
            JSONObject jsonResponse = getParcelTrackingJson(numberTrack);   //выполняем http-запрос
            JSONArray statusesArray = getStatuses(jsonResponse);    //из json получаем список данных о местоположении
            PackageLocation[] packageLocations = new PackageLocation[statusesArray.length()];
            for (int j = 0; j < statusesArray.length(); j++) {  //парсим из списка json в список объектов
                JSONObject statusObject = statusesArray.getJSONObject(j);
                packageLocations[j] = new PackageLocation();
                parser(packageLocations[j], statusObject);
            }
            return packageLocations;
        }catch (IOException| org.apache.hc.core5.http.ParseException ex){
            System.out.println(STR."Ошибка отправки http-запроса: \{ex.getMessage()}");
            return null;
        }
        catch (JSONException e){
            System.out.println(STR."Ошибка парсинга JSON: \{e.getMessage()}");
            return null;
        }
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
