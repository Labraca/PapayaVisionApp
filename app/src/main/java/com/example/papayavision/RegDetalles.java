package com.example.papayavision;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import android.os.Bundle;
import android.widget.TextView;

import com.example.papayavision.DBUtilities.RegRepository;
import com.example.papayavision.entidades.Registro;

public class RegDetalles extends AppCompatActivity {
    private RegRepository regRepo;
    private Registro registroHost;
    private TextView volReg,volEstimado,tempActual,hrelActual,tempPrxSem,hrelPrxSem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_detalles);
        getSupportActionBar().hide();
        regRepo = new RegRepository(getApplication());
        int idReg = getIntent().getIntExtra("idReg", Integer.MIN_VALUE);

        volReg = findViewById(R.id.volActual);
        //volEstimado = findViewById(R.id.volEstimadoLabel);
        //tempActual = findViewById(R.id.tempActual);
       // tempPrxSem = findViewById(R.id.tempPrxSem);
       // hrelActual = findViewById(R.id.hrelActual);
       // hrelPrxSem = findViewById(R.id.hrelPrxSem);

        LiveData<Registro> reg = regRepo.getRegById(idReg);
        reg.observe(this, new Observer<Registro>() {
            @Override
            public void onChanged(Registro registro) {
                registroHost = registro;
                volReg.setText(registroHost.getVolumen()+"t");
            }
        });

    }

}