package com.example.papayavision.DBUtilities;

import android.content.Context;
import android.content.SharedPreferences;

public class QueryPreferencias {

    public static void guardarUbicacion(Context c, String localidad,String CodPostal) {
        SharedPreferences preferences = c.getSharedPreferences("prefs", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("Localidad", localidad);
        editor.putString("CodPostal",CodPostal);
        editor.commit();
    }

    public static String[] cargarUbicacion(Context c) {
        SharedPreferences preferences = c.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        String[] loc_Cod = new String[2];
        loc_Cod[0] = preferences.getString("Localidad","No hay ubicación guardada");
        loc_Cod[1] = preferences.getString("CodPostal","No hay ubicación guardada");
        return loc_Cod;
    }
    public static boolean existeUbi(Context c){
        SharedPreferences preferences = c.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        return preferences.contains("Localidad") && preferences.contains("CodPostal");
    }
}
