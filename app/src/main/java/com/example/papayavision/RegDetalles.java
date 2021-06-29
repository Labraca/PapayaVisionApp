package com.example.papayavision;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.papayavision.DBUtilities.RegRepository;
import com.example.papayavision.entidades.Registro;
import com.example.papayavision.entidades.RegistroViewModel;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;

import org.jetbrains.annotations.NotNull;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

public class RegDetalles extends AppCompatActivity {
    private RegRepository regRepo;
    private Registro registroHost;
    private TextView volReg,volEstimado,tempActual,hrelActual,tempPrxSem,hrelPrxSem;
    private RegistroViewModel viewModel;
    private String TAG="CV";
    private Boolean inflatedOnce = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_detalles);
        getSupportActionBar().hide();

        viewModel = new ViewModelProvider(this).get(RegistroViewModel.class);
        /*viewModel.getSelectedItem().observe(this, item -> {
            // Perform an action with the latest item data
        });*/
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED){

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

                requestPermissions((new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE}),98);

            }
        }
        regRepo = new RegRepository(getApplication());
        int idReg = getIntent().getIntExtra("idReg", Integer.MIN_VALUE);
        //TODO: AQUI ESTA EL PROBLEMA DE QUE SE AÑADA EL LAYOUT
        LiveData<Registro> reg = regRepo.getRegById(idReg);
        reg.observe(this, new Observer<Registro>() {
            @Override
            public void onChanged(Registro registro) {
                registroHost = registro;
                viewModel.selectItem(registro);

                if (savedInstanceState == null && !inflatedOnce) {
                    setInflatedOnce(true);
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .add(R.id.fragmentContainerView, RegDatosFragment.class, null)
                            .commit();
                }
            }
        });

    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 98){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
              //  startcamera();
            }else{
                Toast.makeText(this,"Esta aplicacion necesita saber la localización de su invernadero para su correcto funcionaminento",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    private void setInflatedOnce(Boolean bool){
        this.inflatedOnce = bool;
    }
}