package com.example.papayavision.DBUtilities;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Random;

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
    public static String[] cargarPesos(Context c) {
        SharedPreferences preferences = c.getSharedPreferences("regression", Context.MODE_PRIVATE);
        String[] pesosBias = new String[6];
        Random x =  new Random();
        for (int i = 0;i<pesosBias.length-1;i++){
            double defVal = x.nextDouble();
            pesosBias[i] = preferences.getString("X"+i,Double.toString(defVal));
        }
        pesosBias[6] = preferences.getString("bias",Double.toString(x.nextDouble()));
        return pesosBias;
    }
    public static void guardarPesos(Context c, double[] pesos,double bias) {
        SharedPreferences preferences = c.getSharedPreferences("regression", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        for (int i = 0;i<pesos.length;i++){
            editor.putString("X"+i,Double.toString(pesos[i]));
        }
        editor.putString("bias",Double.toString(bias));
        editor.commit();
    }
}
