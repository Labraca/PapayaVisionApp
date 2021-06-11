package com.example.papayavision;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;

import com.example.papayavision.entidades.Registro;
import com.example.papayavision.entidades.RegistroViewModel;
import com.example.papayavision.regUtilities.Adaptador;



public class RegistrosSemanales extends AppCompatActivity {
    private RegistroViewModel iRegistroViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registros_semanales);

        getSupportActionBar().hide();

        RecyclerView recyclerView = findViewById(R.id.recycled_RegSem);
        final Adaptador adapter = new Adaptador(new Adaptador.RegDiff());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        iRegistroViewModel = new ViewModelProvider(this).get(RegistroViewModel.class);

        iRegistroViewModel.getAllRegSem().observe(this, registros -> {
            adapter.submitList(registros);
        });
    }


}