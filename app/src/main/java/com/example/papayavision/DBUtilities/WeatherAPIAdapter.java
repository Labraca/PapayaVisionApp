package com.example.papayavision.DBUtilities;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.RequestFuture;
import com.example.papayavision.MainActivity;
import com.example.papayavision.entidades.Municipio;
import com.example.papayavision.entidades.Registro;
import com.example.papayavision.regUtilities.WeatherApiUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.Configuration;
import io.swagger.client.api.PrediccionesEspecificasApi;
import io.swagger.client.auth.ApiKeyAuth;
import io.swagger.client.model.Model200;

public class WeatherAPIAdapter {
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbmdlbGxhYnJhY2FAZ21haWwuY29tIiwianRpIjoiMWM5NTJlYTktZjA4ZS00MTFhLTk0NGItYjJhZjA3MzY1NTY0IiwiaXNzIjoiQUVNRVQiLCJpYXQiOjE2MjIyMjM4OTksInVzZXJJZCI6IjFjOTUyZWE5LWYwOGUtNDExYS05NDRiLWIyYWYwNzM2NTU2NCIsInJvbGUiOiIifQ.8mwHhNy-BxJQC4FsFrt6ML7xzNTMtc7fjStMAJao_Hk";
    private static volatile WeatherAPIAdapter INSTANCIA;
    private ApiClient defaultClient;
    private ApiKeyAuth api_key;
    private VolleySingleton volleySingleton;
    private WeatherApiUtils wautils;
    private List<Municipio> municipios;
    private final ExecutorService executorService= Executors.newCachedThreadPool();
    private Context context;
    private RegRepository db;
    private WeatherAPIAdapter(Application application){
        defaultClient= Configuration.getDefaultApiClient();
        // Configure API key authorization: api_key
        api_key = (ApiKeyAuth) defaultClient.getAuthentication("api_key");
        api_key.setApiKey(API_KEY);
        volleySingleton = VolleySingleton.getInstance(application.getApplicationContext());
        db = new RegRepository(application);
        municipios = db.getAllMunicipios();
        context = application.getApplicationContext();
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

    /*private float[] getMedias(String codigoMun){
        JsonObject json = getInfoMunFecha(codigoMun);
        return wautils.getMedias(json);
    }*/
    //TODO
    public void updateMediasRegistro(String codigoMun,Registro reg){

        PrediccionesEspecificasApi apiInstance = new PrediccionesEspecificasApi(); //llamada a la API

        try {
            //Resultado de la llamada a la API
            Model200 result = apiInstance.prediccinPorMunicipiosDiariaTiempoActual_(codigoMun);
            getDatosResponse(result,reg);
        } catch (ApiException e) {
            System.err.println("Exception when calling PrediccionesEspecificas#prediccinPorMunDiaria");
            e.printStackTrace();
        }
    }

    private void getDatosResponse(Model200 m200,Registro reg){
        String json_url = m200.getDatos();
        JsonArrayRequest request =
                new JsonArrayRequest(Request.Method.GET, json_url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.i("Weather","Respuesta obtenida de la API");
                        Gson gson = new Gson();
                        JsonObject jsonObj = null;
                        try {
                            jsonObj = gson.fromJson(response.get(0).toString(),JsonObject.class);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        float[] medias = WeatherApiUtils.getMedias(jsonObj);
                        reg.setTemp(medias[0]);
                        reg.setHrel(medias[1]);
                        db.update(reg);
                        
                        Toast.makeText(context,response.toString(),Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Toast.makeText(context,error.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
        volleySingleton.getRequestQueue().add(request);

    }
    public Municipio getMunicipioByName(String nombre){
        if(this.municipios != null)
            return WeatherApiUtils.getMunicipioByName(municipios,nombre);
        else
            return null;
    }

}
