package com.example.papayavision.regUtilities;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.example.papayavision.DBUtilities.WeatherAPIAdapter;
import com.example.papayavision.entidades.Municipio;
import com.example.papayavision.entidades.Registro;
import com.google.gson.JsonArray;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
//TODO
public class WeatherApiUtils {

    public Municipio getMunicipioByName(List<Municipio> m, String s){
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();

        Comparator<Municipio> comparator =
                Comparator.comparingInt(
                        p -> levenshteinDistance.apply(p.getMunicipio(), s));

        Municipio minMunicipio = Collections.min(m, comparator);
        return minMunicipio;

    }
    //TODO
    public void getMedias(Registro reg, Municipio m, Date fechaInicio, WeatherAPIAdapter weatherAPIAdapter){
        JSONArray datos = weatherAPIAdapter.getInfoMunFecha(m,fechaInicio);

        int hrelMedia=0;
        float tempMedia=0;

        for (int i=0;i<datos.length();i++){
            JSONObject dia = null;
            try {
                dia = (JSONObject) datos.get(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
           // hrelMedia += dia.getInt("h")
        }
    }


}
