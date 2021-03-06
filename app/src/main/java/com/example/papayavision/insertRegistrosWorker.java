package com.example.papayavision;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.papayavision.DBUtilities.AppDatabase;
import com.example.papayavision.DBUtilities.QueryPreferencias;
import com.example.papayavision.DBUtilities.RegRepository;
import com.example.papayavision.entidades.Registro;
import com.example.papayavision.regUtilities.MultipleLinearRegression;
import com.google.common.escape.Escaper;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.example.papayavision.DBUtilities.QueryPreferencias.cargarUbicacion;

public class insertRegistrosWorker extends Worker {
    private AppDatabase db;

    public insertRegistrosWorker(@NonNull @NotNull Context context, @NonNull @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
        db = AppDatabase.getDatabase((Application)context.getApplicationContext());
    }

    @NonNull
    @NotNull
    @Override
    public Result doWork() {
        return insertNuevosReg();
    }

    //Método para insertar nuevo registros
    private Result insertNuevosReg() {
        Log.w("WORKER", "Insertando nuevos registros...");
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(cal.MONDAY);
        int diffDays = cal.getFirstDayOfWeek() - cal.get(Calendar.DAY_OF_WEEK);
        cal.add(Calendar.DAY_OF_WEEK, diffDays);

        String[] ubicacion = QueryPreferencias.cargarUbicacion(getApplicationContext());
        Registro reg = new Registro(0, cal.getTime());


        Registro last;
        try {
            last = db.registroDao().getLastRegistroNow();
        } catch (Exception e) {
            db.registroDao().insertRegistros(reg);
            Log.w("WORKER", "No se ha podido extraer el ultimo registro se intentará mas tarde");
            return Result.retry();
        }

        if (weeksUntilLastConnection(last) < 1) {
            Log.w("WORKER", "No ha pasado mas de una semana desde el ultimo registro");
            return Result.success();
        }
        if (last.getHrel() < 0 && !ubicacion[1].equals("-1"))
            db.wAPIAdapter.updateMediasRegistro(ubicacion[1], last);


        if(last.getVolumen() == -1){
            Log.w("WORKER","Volumen negativos");
            return Result.retry();
        }else{
            double[] features = new double[8];
            features[0] = last.getTemp();
            features[1] = last.getHrel();
            features[2] = last.getPerm25();
            features[3] = last.getPer25_33();
            features[4] = last.getPer33_50();
            features[5] = last.getPer50_70();
            features[6] = last.getPer70();
            cal.setTime(last.getInicioFecha());
            features[7] = cal.get(Calendar.MONTH);
            double[] result = {last.getVolumen()};

            ArrayList<double[]> datos = new ArrayList<double[]>();
            datos.add(features);

            MultipleLinearRegression mlr = MultipleLinearRegression.getINSTANCIA(getApplicationContext());
            //Entreno el modelo linear de regresion
            mlr.fit(datos,result,1,8,getApplicationContext());

            //calculo la nueva estimacion para esta semana (usando los porcentajes de la ultima semana)
            double[] datosActuales = new double[8];
            datosActuales[0] = reg.getTemp();
            datosActuales[1] = reg.getHrel();
            datosActuales[2] = last.getPerm25();
            datosActuales[3] = last.getPer25_33();
            datosActuales[4] = last.getPer33_50();
            datosActuales[5] = last.getPer50_70();
            datosActuales[6] = last.getPer70();
            cal.setTime(reg.getInicioFecha());
            datosActuales[7] = cal.get(Calendar.MONTH);

            int estimacion = mlr.calcularVolumenEstimado(datosActuales);
            //guardo la estimacion
            QueryPreferencias.guardarEstimacion(getApplicationContext(),estimacion);

        }
        try {
            db.registroDao().insertRegistros(reg);
        }catch (Exception e){
            return Result.failure();
        }
        if(!ubicacion[1].equals("-1")){
            if (isRegInserted(reg))
                db.wAPIAdapter.updateMediasRegistro(ubicacion[1], reg);
            else
                while (!isRegInserted(reg)) {
                    db.wAPIAdapter.updateMediasRegistro(ubicacion[1], reg);
                }
        }
        Log.i("WORKER","Registro añadido");
        return Result.success();
    }

    //Calcular cuantas semanas hace desde el ultimo registro
    private int weeksUntilLastConnection(Registro last){
        Date currentDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(cal.MONDAY);
        int currentADias = (int) TimeUnit.MILLISECONDS.toDays(currentDate.getTime());
        int lastConnectADias = (int) TimeUnit.MILLISECONDS.toDays(last.getInicioFecha().getTime());

        int weeksSinceConnection = (currentADias - lastConnectADias) / 7;
        return weeksSinceConnection;
    }
    private boolean isRegInserted(Registro reg){
        try {
            db.registroDao().getRegById(reg.getIdRegistro());
        }catch (Exception e){
            return false;
        }
        return true;
    }
}
