package com.example.papayavision.DBUtilities;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

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
import com.example.papayavision.entidades.MunicipioDAO;
import com.example.papayavision.regUtilities.WeatherApiUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import io.swagger.client.*;
import io.swagger.client.api.ValoresClimatologicosApi;
import io.swagger.client.auth.*;
import io.swagger.client.model.Model200;
import io.swagger.client.api.PrediccionesEspecificasApi;
public class WeatherAPIAdapter {
    private static String API_KEY = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbmdlbGxhYnJhY2FAZ21haWwuY29tIiwianRpIjoiMWM5NTJlYTktZjA4ZS00MTFhLTk0NGItYjJhZjA3MzY1NTY0IiwiaXNzIjoiQUVNRVQiLCJpYXQiOjE2MjIyMjM4OTksInVzZXJJZCI6IjFjOTUyZWE5LWYwOGUtNDExYS05NDRiLWIyYWYwNzM2NTU2NCIsInJvbGUiOiIifQ.8mwHhNy-BxJQC4FsFrt6ML7xzNTMtc7fjStMAJao_Hk";
    private static volatile WeatherAPIAdapter INSTANCIA;
    private ApiClient defaultClient;
    private ApiKeyAuth api_key;
    private RequestQueue queue;
    private WeatherApiUtils wautils;
    private List<Municipio> municipios;

    private WeatherAPIAdapter(Application application){
        defaultClient= Configuration.getDefaultApiClient();
        // Configure API key authorization: api_key
        api_key = (ApiKeyAuth) defaultClient.getAuthentication("api_key");
        api_key.setApiKey(API_KEY);
        queue = Volley.newRequestQueue(application);
        RegRepository regRepository = new RegRepository(application);
        municipios = regRepository.getAllMunicipios();

    }

    public static WeatherAPIAdapter getWeatherAPIAdapter(Application application) {
        if (INSTANCIA == null) {
            synchronized (WeatherAPIAdapter.class) {
                if (INSTANCIA == null) {
                    INSTANCIA = new WeatherAPIAdapter(application);
                }
            }
        }
        return INSTANCIA;
    }
    public float[] getMedias(String codigoMun){
        JsonObject json = getInfoMunFecha(codigoMun);
        return wautils.getMedias(json);
    }
    //TODO
    private JsonObject getInfoMunFecha(String codigoMun){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        PrediccionesEspecificasApi apiInstance = new PrediccionesEspecificasApi();

        JsonObject responseFinal = null;
        try {
            Model200 result = apiInstance.prediccinPorMunicipiosDiariaTiempoActual_(codigoMun);
            responseFinal = getDatosResponse(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ValoresClimatologicosApi#climatologasDiarias1");
            e.printStackTrace();
        }

        return responseFinal;
    }

    private JsonObject getDatosResponse(Model200 m200){
        String json_url = m200.getDatos();
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request =
                new JsonObjectRequest(Request.Method.GET,json_url,null,future,future);
        Gson gson = new Gson();
        queue.add(request);
        JsonObject response = null;
        try {
            response = gson.fromJson(future.get().toString(),JsonObject.class); // this will block
        } catch (InterruptedException e) {
            // exception handling
            e.printStackTrace();
        } catch (ExecutionException e) {
            // exception handling
            e.printStackTrace();
        }

        return response;
    }
    public Municipio getMunicipioByName(String nombre){
        if(this.municipios != null)
            return WeatherApiUtils.getMunicipioByName(municipios,nombre);
        else
            return null;
    }
}
