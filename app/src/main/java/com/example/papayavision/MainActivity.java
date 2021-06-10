package com.example.papayavision;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.example.papayavision.DBUtilities.QueryPreferencias;
import com.example.papayavision.DBUtilities.RegRepository;
import com.example.papayavision.entidades.Registro;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import org.jetbrains.annotations.NotNull;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_COARSE_LOCATION = 99 ;
    private RegRepository db;
    //Google api para saber onde estamos
    FusedLocationProviderClient fusedlocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        if(!QueryPreferencias.existeUbi(getApplicationContext())) {
            //crear archivo
            updateGPS();
        }

        db = new RegRepository(getApplication());
        LiveData<Registro> last = db.getLast();
        last.observe(this, new Observer<Registro>() {
            @Override
            public void onChanged(Registro registro) {
                insertNuevosReg(registro);
            }
        });
    }//end create

    private void updateGPS(){
        //Validar permisos de gps
        //conseguir la localizacion etc

        fusedlocation = LocationServices.getFusedLocationProviderClient(this);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){

            fusedlocation.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    //we got permisinon,put values.
                    putGeocode(location);
                }
            });
        }else{

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

                requestPermissions((new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}),PERMISSION_COARSE_LOCATION);

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 99){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                updateGPS();
            }else{
                Toast.makeText(this,"Esta aplicacion necesita saber la localización de su invernadero para su correcto funcionaminento",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void putGeocode(Location location) {

        Geocoder geocoder = new Geocoder(this);

        try {

            List<Address> addresses = geocoder
                    .getFromLocation(location.getLatitude(),location.getLongitude(),1);

            QueryPreferencias.guardarUbicacion(getApplicationContext(),addresses.get(0).getLocality());

        }catch (Exception e){

            QueryPreferencias
                    .guardarUbicacion(getApplicationContext(),"No se consiguió determinar su UbicacionFragment");

        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        LiveData<Registro> last = db.getLast();

        last.observe(this, new Observer<Registro>() {
            @Override
            public void onChanged(Registro registro) {
                insertNuevosReg(registro);
            }
        });
    }

    private void insertNuevosReg(Registro last) {
        Date currentDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(cal.MONDAY);
        Date lastConnection = last.getInicioFecha();
        cal.setTime(lastConnection);
        int currentADias = (int) TimeUnit.MILLISECONDS.toDays(currentDate.getTime());
        int lastConnectADias = (int) TimeUnit.MILLISECONDS.toDays(lastConnection.getTime());

        int weeksSinceConnection = (currentADias - lastConnectADias) / 7;

        for (int i = 0; i < weeksSinceConnection; i++) {
            cal.add(Calendar.DAY_OF_WEEK, 7);
            Registro reg = new Registro(0, cal.getTime());
            db.insert(reg);
        }
        /*else{
            int diffDays = cal.getFirstDayOfWeek() -  cal.get(Calendar.DAY_OF_WEEK);
            cal.add(Calendar.DAY_OF_WEEK,diffDays);
            Registro reg = new Registro(0,cal.getTime());
            db.insert(reg);
        }*/
    }

    public void launchActivity(View v){
        Intent i = null;
        switch(v.getId()){
            case R.id.toPhoto:
                //a camara
                //startActivity(i);
            case R.id.toRegSem:
                i = new Intent(getApplicationContext(),RegistrosSemanales.class);
                startActivity(i);
            case R.id.toRegSemAct:
                //al primer registro
                //startActivity(i);
        }
    }

}