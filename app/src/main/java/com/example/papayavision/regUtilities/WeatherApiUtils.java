package com.example.papayavision.regUtilities;

import com.example.papayavision.entidades.Municipio;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

//TODO
public class WeatherApiUtils {

    public static Municipio getMunicipioByName(List<Municipio> m, String s){

        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();

        Comparator<Municipio> comparator =
                Comparator.comparingInt(
                        p -> levenshteinDistance.apply(p.getMunicipio(), s));

        Municipio minMunicipio = Collections.min(m, comparator);

        return minMunicipio;

    }
    //TODO
    public static float[] getMedias(JsonObject datos){
        JsonArray dias = new JsonArray();

        dias = (JsonArray) (((JsonObject)datos.get("prediccion")).get("dia"));

        float hrelMedia=0;
        float tempMedia=0;
        Iterator<JsonElement> iterator = dias.iterator();
        int i = 0;
        while(iterator.hasNext()) {
            JsonObject dia = (JsonObject) iterator.next();
            JsonObject temperatura = (JsonObject) dia.get("temperatura");
            JsonObject humedad = (JsonObject) dia.get("humedadRelativa");

            float maxTemp = Float.parseFloat(temperatura.get("maxima").toString());
            float minTemp = Float.parseFloat(temperatura.get("minima").toString());

            float maxHrel = Float.parseFloat(humedad.get("maxima").toString());
            float minHrel = Float.parseFloat(humedad.get("minima").toString());

            float mediaTemp = (minTemp + maxTemp) / 2;
            float mediaHrel = (minHrel + maxHrel) / 2;

            tempMedia = tempMedia + (mediaTemp - tempMedia) / (++i);
            hrelMedia = hrelMedia + (mediaHrel - hrelMedia) / (i);
        }
        float[] medias = {tempMedia,hrelMedia};
        return medias;
    }


}
