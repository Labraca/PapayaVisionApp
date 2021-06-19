package com.example.papayavision.DBUtilities;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.papayavision.entidades.Municipio;
import com.example.papayavision.regUtilities.WeatherApiUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import io.swagger.client.*;
import io.swagger.client.api.ValoresClimatologicosApi;
import io.swagger.client.auth.*;
import io.swagger.client.model.Model200;

public class WeatherAPIAdapter {
    private static String API_KEY = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbmdlbGxhYnJhY2FAZ21haWwuY29tIiwianRpIjoiMWM5NTJlYTktZjA4ZS00MTFhLTk0NGItYjJhZjA3MzY1NTY0IiwiaXNzIjoiQUVNRVQiLCJpYXQiOjE2MjIyMjM4OTksInVzZXJJZCI6IjFjOTUyZWE5LWYwOGUtNDExYS05NDRiLWIyYWYwNzM2NTU2NCIsInJvbGUiOiIifQ.8mwHhNy-BxJQC4FsFrt6ML7xzNTMtc7fjStMAJao_Hk";
    private static volatile WeatherAPIAdapter INSTANCIA;
    private ApiClient defaultClient;
    private ApiKeyAuth api_key;
    private RequestQueue queue;
    private WeatherApiUtils wautils;
    private WeatherAPIAdapter(Context c){
        defaultClient= Configuration.getDefaultApiClient();
        // Configure API key authorization: api_key
        api_key = (ApiKeyAuth) defaultClient.getAuthentication("api_key");
        api_key.setApiKey(API_KEY);
        queue = Volley.newRequestQueue(c);
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
    //TODO
    public JSONArray getInfoMunFecha(Municipio m, Date fechaInicio){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        Calendar cal = Calendar.getInstance();
        cal.setTime(fechaInicio);
        cal.add(Calendar.DAY_OF_WEEK,6);
        ValoresClimatologicosApi apiInstance = new ValoresClimatologicosApi();

        String fechaIniStr = sdf.format(fechaInicio); // String | Fecha Inicial (AAAA-MM-DDTHH:MM:SSUTC)
        String fechaFinStr = sdf.format(cal.getTime()); // String | Fecha Final (AAAA-MM-DDTHH:MM:SSUTC)

        JSONArray responseFinal = null;

        try {
            Model200 result = apiInstance.climatologasDiarias1(fechaIniStr, fechaFinStr);
            responseFinal = getDatosResponse(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ValoresClimatologicosApi#climatologasDiarias1");
            e.printStackTrace();
        }

        return responseFinal;
    }

    private JSONArray getDatosResponse(Model200 m200){
        String json_url = m200.getDatos();
        RequestFuture<JSONArray> future = RequestFuture.newFuture();
        JsonArrayRequest request = new JsonArrayRequest(json_url,future,future);
        queue.add(request);
        JSONArray response = null;
        try {
            response = future.get(); // this will block
        } catch (InterruptedException e) {
            // exception handling
            e.printStackTrace();
        } catch (ExecutionException e) {
            // exception handling
            e.printStackTrace();
        }

        return response;
    }

}
