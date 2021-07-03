package com.example.papayavision;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import com.example.papayavision.DBUtilities.QueryPreferencias;
import com.example.papayavision.DBUtilities.RegRepository;
import com.example.papayavision.DBUtilities.WeatherAPIAdapter;
import com.example.papayavision.entidades.Municipio;
import com.example.papayavision.entidades.Registro;
import com.example.papayavision.regUtilities.WeatherApiUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;

import org.jetbrains.annotations.NotNull;
import org.opencv.android.OpenCVLoader;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_COARSE_LOCATION = 99 ;
    private RegRepository db;
    private TextView ubiState;
    //Google api para saber onde estamos
    private FusedLocationProviderClient fusedlocation;
    private AutoCompleteTextView municipioCompleteView;
    private WeatherAPIAdapter wApiAdapter;
    private String[] ubi;
    private Registro lastReg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        staticLoadCVLibraries();

        wApiAdapter = WeatherAPIAdapter.getWeatherAPIAdapter(getApplication());
        db = new RegRepository(getApplication());

        ubi = QueryPreferencias.cargarUbicacion(getApplicationContext());
        ubiState = findViewById(R.id.ubicacionState);

        if(!(!QueryPreferencias.existeUbi(getApplicationContext()))
                || (ubi[0].equals("No hay ubicación guardada"))) {
            updateGPS();
            ubiState.setText(ubi[0]);
        }else{
            ubiState.setText(ubi[0]);
        }

        //Autocomplete
        municipioCompleteView = (AutoCompleteTextView) findViewById(R.id.municipioCompleteView);
        municipioCompleteView.setThreshold(1);
        municipioCompleteView.setValidator(new AutoCompleteTextView.Validator() {
            @Override
            public boolean isValid(CharSequence text) {
                return (text.length() != 0);
            }

            @Override
            public CharSequence fixText(CharSequence invalidText) {
                return null;
            }
        });

        LiveData<List<String>> municipios = db.getAllNombreMuni();
        municipios.observe(this, new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> strings) {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplication(),
                        android.R.layout.simple_expandable_list_item_1, strings);
                municipioCompleteView.setAdapter(adapter);

            }
        });

        LiveData<Registro> last = db.getLast();
        last.observe(this, new Observer<Registro>() {
            @Override
            public void onChanged(Registro registro) {
                lastReg = registro;
            }
        });
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);

        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysFromMonday = dayOfWeek - cal.getFirstDayOfWeek();

        PeriodicWorkRequest insertRegistros = new PeriodicWorkRequest.Builder(
                    insertRegistrosWorker.class, 7, TimeUnit.DAYS)
                    .setInitialDelay(7-daysFromMonday,TimeUnit.DAYS)
                    .addTag("insertRegistrosMonday")
                    .build();

        WorkManager.getInstance(getApplicationContext())
                .enqueueUniquePeriodicWork("insertRegistrosMonday",
                        ExistingPeriodicWorkPolicy.KEEP,insertRegistros);

    }//end create

    //TODO
    public void launchActivity(View v){
        if(!(!QueryPreferencias.existeUbi(getApplicationContext())
        || lastReg == null
        || lastReg.getTemp() == -1.0))
            showPopUp(v);
        else {
            Intent i = null;
            switch (v.getId()) {
                case R.id.toPhoto:
                    //a camara
                    //startActivity(i);
                case R.id.toRegSem:
                    i = new Intent(getApplicationContext(), RegistrosSemanales.class);
                    startActivity(i);
                case R.id.toRegSemAct:
                    //al primer registro
                    //startActivity(i);
            }
        }
    }
    private void staticLoadCVLibraries() {
        boolean load = OpenCVLoader.initDebug();
        if(load){
            Log.i("CV","Open CV Libraries loaded.");
        }
    }
    public void setUbication(View v){
        String municipio = municipioCompleteView.getText().toString();

        Municipio m = wApiAdapter.getMunicipioByName(municipio);
        QueryPreferencias.guardarUbicacion(getApplicationContext(),
                municipio,m.getCodMunicipio()+"");

        ubiState.setText(municipio);

    }
    public void updateGPSP(View view){
        updateGPS();
    }

    private void updateGPS(){
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        //Validar permisos de gps
        //conseguir la localizacion etc
        if(fusedlocation == null)
            fusedlocation = LocationServices.getFusedLocationProviderClient(this);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){

            fusedlocation.getCurrentLocation(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, new CancellationToken() {
                @Override
                public boolean isCancellationRequested() {
                    return false;
                }

                @NonNull
                @NotNull
                @Override
                public CancellationToken onCanceledRequested(@NonNull @NotNull OnTokenCanceledListener onTokenCanceledListener) {
                    return null;
                }
            }).addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    putGeocode(location);
                }
            });
        }else{

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

                requestPermissions((new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}),PERMISSION_COARSE_LOCATION);

            }
        }
        findViewById(R.id.progressBar).setVisibility(View.GONE);
    }
    private void putGeocode(Location location) {

        Geocoder geocoder = new Geocoder(this);
        String[] ubicacion;
        try {

            List<Address> addresses = geocoder
                    .getFromLocation(location.getLatitude(),location.getLongitude(),1);
            Address address = addresses.get(0);
            Municipio m = wApiAdapter.getMunicipioByName(address.getLocality());
            QueryPreferencias.guardarUbicacion(getApplicationContext(),m.getMunicipio(),m.getCodMunicipio()+"");

            ubiState.setText(m.getMunicipio());
        }catch (Exception e){

            QueryPreferencias
                    .guardarUbicacion(getApplicationContext(),"No se consiguió determinar su ubicación","");
            ubiState.setText("No se consiguió determinar su ubicación");
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
    private void showPopUp(View v){
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
        // dismiss the popup window when touched

        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }

        });
    }



}