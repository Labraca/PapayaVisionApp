package com.example.papayavision.DBUtilities;

import android.content.Context;

import io.swagger.client.*;
import io.swagger.client.auth.*;

public class WeatherAPIAdapter {
    private static String API_KEY = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbmdlbGxhYnJhY2FAZ21haWwuY29tIiwianRpIjoiMWM5NTJlYTktZjA4ZS00MTFhLTk0NGItYjJhZjA3MzY1NTY0IiwiaXNzIjoiQUVNRVQiLCJpYXQiOjE2MjIyMjM4OTksInVzZXJJZCI6IjFjOTUyZWE5LWYwOGUtNDExYS05NDRiLWIyYWYwNzM2NTU2NCIsInJvbGUiOiIifQ.8mwHhNy-BxJQC4FsFrt6ML7xzNTMtc7fjStMAJao_Hk";
    private static volatile WeatherAPIAdapter INSTANCIA;
    private ApiClient defaultClient;
    private ApiKeyAuth api_key;
    private WeatherAPIAdapter(Context c){
        defaultClient= Configuration.getDefaultApiClient();
        // Configure API key authorization: api_key
        api_key = (ApiKeyAuth) defaultClient.getAuthentication("api_key");
        api_key.setApiKey(API_KEY);

    }
    public WeatherAPIAdapter getWeatherAPIAdapter(Context context) {
        if (INSTANCIA == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCIA == null) {
                    INSTANCIA = new WeatherAPIAdapter(context);
                }
            }
        }
        return INSTANCIA;
    }

}
