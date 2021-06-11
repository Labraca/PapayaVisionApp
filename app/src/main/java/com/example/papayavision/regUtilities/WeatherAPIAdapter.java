package com.example.papayavision.regUtilities;

import android.content.Context;

import androidx.room.Room;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.papayavision.DBUtilities.AppDatabase;

import org.json.JSONObject;

public class WeatherAPIAdapter {

    private static volatile WeatherAPIAdapter INSTANCIA;
    private static Context ctx;
    private RequestQueue requestQueue;
    private static String getUrl="https://www.el-tiempo.net/api/json/v2/provincias/";
    private WeatherAPIAdapter(Context c){
        ctx = c;
        requestQueue = Volley.newRequestQueue(ctx);
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

    public JSONObject getWeatherOfReg(String CodPostal){
        String url = getUrl+"/"+CodPostal.substring(0,1)+"/municipios/"+CodPostal;
        final JSONObject[] respuesta = new JSONObject[1];
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        respuesta[0] = response;
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //TODO
                    }

                });
        requestQueue.add(jsonObjectRequest);
        return respuesta[0];
    }

}
