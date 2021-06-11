package com.example.papayavision.DBUtilities;

import android.content.Context;
import android.content.SharedPreferences;

public class QueryPreferencias {

    public static void guardarUbicacion(Context c, String ubicacion) {
        SharedPreferences preferences = c.getSharedPreferences("prefs", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("UbicacionFragment", ubicacion);
        editor.commit();
    }

    public static String cargarUbicacion(Context c) {
        SharedPreferences preferences = c.getSharedPreferences("prefs", Context.MODE_PRIVATE);

        return preferences.getString("UbicacionFragment","No hay ubicación guardada");
    }
    public static boolean existeUbi(Context c){
        SharedPreferences preferences = c.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        return preferences.contains("UbicacionFragment");
    }
}
