package com.example.papayavision;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.example.papayavision.DBUtilities.QueryPreferencias;
import com.example.papayavision.DBUtilities.RegRepository;

public class Ajustes extends AppCompatActivity {
    private EditText tamanoFinca,numeroArboles;
    private Button guardar;
    private double Za2 = 3.842;
    private RegRepository repo;
    private AutoCompleteTextView variedad;
    private final String[] VARIEDADES = {"Intenzza","Gradted Intenzza","Sweet Sense","Vitale", "Caballero","Alicia"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajustes);
        getSupportActionBar().hide();
        String[] ajustes = QueryPreferencias.cargarAjustes(getApplicationContext());

        repo = new RegRepository(getApplication());

        tamanoFinca = (EditText) findViewById(R.id.tamanoText);
        numeroArboles = (EditText) findViewById(R.id.numeroText);
        variedad = (AutoCompleteTextView) findViewById(R.id.municipioCompleteView);
        variedad.setThreshold(1);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplication(),
                android.R.layout.simple_expandable_list_item_1, VARIEDADES);
        variedad.setAdapter(adapter);

        if(!ajustes[0].equals("-1"))
            tamanoFinca.setText(ajustes[0]);
        if(!ajustes[1].equals("-1"))
            numeroArboles.setText(ajustes[1]);

        guardar = (Button) findViewById(R.id.guardarAjustes);

        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tamano = tamanoFinca.getText().toString();
                String arb = numeroArboles.getText().toString();
                String variedadText = variedad.getText().toString();
                int numFotos = 0;
                if(!arb.isEmpty()){
                    int numArb = Integer.parseInt(arb);
                    numFotos = estimarNumFotos(numArb);

                }else if(!tamano.isEmpty()){
                    double tamanoDoub = Double.parseDouble(arb);
                    numFotos = estimarNumFotos(tamanoDoub);
                }
                QueryPreferencias.guardarAjustes(getApplicationContext(),tamano,arb,variedadText,numFotos);
            }
        });
    }

    public int estimarNumFotos(double tamano){

        double nfruits,yield,weight;

        switch (variedad.getText().toString()){
            case "Intenzza":
                weight = 1.85;
                yield = 9.59;
                nfruits = 24.90;
                break;
            case "Grafted Intenzza":
                weight = 1.85;
                yield = 14.20;
                nfruits = 42.43;
                break;
            case "Sweet Sense":
                weight = 1.55;
                yield = 12.58;
                nfruits = 43.67;
                break;
            case "Vitale":
                weight = 1;
                yield = 4.82;
                nfruits = 11.97;
                break;
            case "Alicia":
                weight = 0.925;
                yield = 12.38;
                nfruits = 39.65;
                break;
            case "Caballero":
                weight = 0.785;
                yield = 11.02;
                nfruits = 40.2;
                break;
            default:
                weight = 1.32;
                yield = 10.765;
                nfruits = 33.8;
                break;
        }

        double numeroArbM2 = yield / (weight * nfruits);

        int numeroArb = (int)(numeroArbM2 * tamano);

        return estimarNumFotos(numeroArb);
    }
    public int estimarNumFotos(int numeroArb){

        double e2 = 25; // error^2
        double pq = 0.25; //p * q

        //tamaño de la muestra
        int numFotos = (int) ((int)(e2 * Za2 * numeroArb * pq)/((e2*(numeroArb-1))+(Za2*pq)));

        return numFotos;
    }

}