package com.example.papayavision.DBUtilities;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Random;

public class QueryPreferencias {

    public static void guardarUbicacion(Context c, String localidad,String CodPostal) {
        SharedPreferences preferences = c.getSharedPreferences("prefs", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("Localidad", localidad);
        editor.putString("CodMun",CodPostal);
        editor.commit();
    }

    public static String[] cargarUbicacion(Context c) {
        SharedPreferences preferences = c.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        String[] loc_Cod = new String[2];
        loc_Cod[0] = preferences.getString("Localidad","No hay ubicación guardada");
        loc_Cod[1] = preferences.getString("CodMun","-1");
        return loc_Cod;
    }
    public static boolean existeUbi(Context c){
        SharedPreferences preferences = c.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        return preferences.contains("Localidad")
                && preferences.contains("CodMun")
                && !cargarUbicacion(c)[1].equals("-1");
    }
    public static String[] cargarPesos(Context c) {
        SharedPreferences preferences = c.getSharedPreferences("regression", Context.MODE_PRIVATE);
        String[] pesosBias = new String[6];
        Random x =  new Random();
        for (int i = 0;i<pesosBias.length-1;i++){
            double defVal = x.nextDouble();
            pesosBias[i] = preferences.getString("X"+i,Double.toString(defVal));
        }
        pesosBias[5] = preferences.getString("bias",Double.toString(x.nextDouble()));
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
    public static void guardarEstimacion(Context c,int estimacion) {
        SharedPreferences preferences = c.getSharedPreferences("estimacion", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("estimacion",Integer.toString(estimacion));
        editor.commit();
    }
    public static String cargarEstimacion(Context c) {
        SharedPreferences preferences = c.getSharedPreferences("estimacion", Context.MODE_PRIVATE);

        return  preferences.getString("estimacion","0");
    }
    public static void guardarAjustes(Context c,String tamañoFinca,String numeroArboles,String variedad,int numFotos) {
        SharedPreferences preferences = c.getSharedPreferences("ajustes", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("tamanoFinca",""+tamañoFinca);
        editor.putString("numeroArboles",""+numeroArboles);
        editor.putString("numeroFotos",""+numFotos);
        editor.putString("variedad",variedad);
        editor.commit();
    }
    public static String[] cargarAjustes(Context c) {
        SharedPreferences preferences = c.getSharedPreferences("ajustes", Context.MODE_PRIVATE);

        String[] ajustes = new String[4];
        ajustes[0] = preferences.getString("tamanoFinca",-1+"");
        ajustes[1] = preferences.getString("numeroArboles",-1+"");
        ajustes[2] = preferences.getString("variedad","");
        ajustes[3] = preferences.getString("numeroFotos",-1+"");

        return ajustes;
    }
}
