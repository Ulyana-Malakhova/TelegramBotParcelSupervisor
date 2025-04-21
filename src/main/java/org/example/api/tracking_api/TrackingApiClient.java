package org.example.api.tracking_api;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.example.Dto.PackageDto;
import org.example.api.PackageLocation;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.ServiceLoader.Provider;

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
     */
    public JSONObject getParcelTrackingJson(String numberTrack) throws IOException {
        RequestConfig requestConfig = RequestConfig.custom()    //настройка параметров http-запроса
                .setSocketTimeout(40 * 1000) // таймаут ответа в миллисекундах
                .setConnectTimeout(60 * 1000) // таймаут соединения в миллисекундах
                .build();
        // Настройка менеджера соединений
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        CloseableHttpClient httpClient = HttpClients.custom() //объект выполнения http-запроса
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connectionManager)
                .build();

        HttpGet request = new HttpGet(url + numberTrack);
        HttpResponse response = httpClient.execute(request);    // Выполнение запроса
        int statusCode = response.getStatusLine().getStatusCode();    //проверка статуса ответа
        if (statusCode != 200) {
            throw new IOException("HTTP error - "+statusCode);
        }
        String jsonResponseString = EntityUtils.toString(response.getEntity()); // Обработка ответа
        JSONObject jsonResponse = new JSONObject(jsonResponseString);
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
        } catch (IOException ex) {
            System.out.println("Ошибка отправки http-запроса: "+ex.getMessage());
            return null;
        } catch (JSONException e) {
            System.out.println("Ошибка парсинга JSON: "+e.getMessage());
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
        }catch (IOException ex){
            System.out.println("Ошибка отправки http-запроса: "+ex.getMessage());
            return null;
        }
        catch (JSONException e){
            System.out.println("Ошибка парсинга JSON: "+e.getMessage());
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

    /**
     * Метод для получения данных об отправке/получении посылки
     * @param packageDto dto для записи данных о посылке
     * @throws IOException статус ответа не OK, ошибка сети или проблемы с подключением
     * @throws ParseException ошибка в парсинге даты
     */
    public abstract void receivingDeliveryData(PackageDto packageDto) throws IOException, ParseException;
    public static List<TrackingApiClient> getServices(ModuleLayer layer) {
        return ServiceLoader
                .load(layer, TrackingApiClient.class)
                .stream()
                .map(Provider::get)
                .collect(Collectors.toList());
    }
}
