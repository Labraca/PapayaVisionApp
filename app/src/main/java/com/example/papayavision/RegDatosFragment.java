package com.example.papayavision;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.papayavision.DBUtilities.QueryPreferencias;
import com.example.papayavision.entidades.Foto;
import com.example.papayavision.entidades.Registro;
import com.example.papayavision.entidades.RegistroViewModel;
import com.example.papayavision.regUtilities.FotoAdapter;
import com.example.papayavision.regUtilities.MultipleLinearRegression;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class RegDatosFragment extends Fragment {
    private RegistroViewModel viewModel;
    private EditText volReg;
    private LiveData<Registro> regHost;
    private LiveData<List<Foto>> fotos;
    private TextView volEstimacion;
    private String estimacion;
    private ConstraintLayout fotosContainer;
    private TextView fotosTotal;
    private FotoAdapter adapter;
    private TextView hrelPrxSem,tempPrxSem;
    public RegDatosFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reg_datos, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (viewModel == null)
            viewModel = new ViewModelProvider(requireActivity()).get(RegistroViewModel.class);
        volReg = view.findViewById(R.id.volActual);
        volEstimacion = view.findViewById(R.id.volEstimacion);
        fotosTotal = view.findViewById(R.id.fotosRecom);
        tempPrxSem = view.findViewById(R.id.tempPrxSem);
        hrelPrxSem = view.findViewById(R.id.hrelPrxSem);

        RecyclerView recyclerView = view.findViewById(R.id.fotosRecycler);
        int numberOfColumns = 3;
        recyclerView.setLayoutManager(new GridLayoutManager(this.getContext(),numberOfColumns));

        regHost = viewModel.getSelectedItem();
        regHost.observe(getViewLifecycleOwner(), new Observer<Registro>() {
            @Override
            public void onChanged(Registro registro) {
                volReg.setText(registro.getVolumen()+"");
                updateEstimacion(registro);
                volEstimacion.setText(estimacion);

                tempPrxSem.setText(registro.getTemp()+"");
                hrelPrxSem.setText(registro.getHrel()+"");

                String[] ajustes = QueryPreferencias.cargarAjustes(getContext());

                if(ajustes[3].isEmpty())
                    fotosTotal
                            .setText("Configure sus ajustes");
                else
                    fotosTotal
                            .setText(viewModel.getNumOfFotosInRegistro(registro)
                            +"/"+ajustes[3]);
                fotos = viewModel.getFotosOfRegLive(registro);
                fotos.observe(getViewLifecycleOwner(), new Observer<List<Foto>>() {
                    @Override
                    public void onChanged(List<Foto> fotos) {
                        adapter = new FotoAdapter(fotos);
                        recyclerView.setAdapter(adapter);
                    }
                });
            }
        });

        volReg.addTextChangedListener(new TextWatcher() {
            private Pattern regex = Pattern.compile("\\d{1,5}");
            private String previousText = "";
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
               String w = s.toString();
                if(regex.matcher(w).matches())
                    previousText = w;
                else
                    s.replace(0,s.length(),previousText);
            }
        });
        volReg.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    String editText = volReg.getText().toString();
                    String volumen = editText.substring(0,editText.length()-2);
                    Registro reg = regHost.getValue();
                    reg.setVolumen(Integer.parseInt(volumen));
                    viewModel.update(reg);
                }
            }
        });

        // Donde clicar para ir a echar fotos
        fotosContainer = view.findViewById(R.id.fotosPorcentajeContainer);
        fotosContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraFragment fragment = new CameraFragment();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack if needed
                transaction.replace(R.id.fragmentContainerView, fragment,null);
                transaction.setReorderingAllowed(true);
                transaction.addToBackStack(null);

                // Commit the transaction
                transaction.commit();
            }
        });
        estimacion = QueryPreferencias.cargarEstimacion(getContext());








    }

    @Override
    public void onResume() {
        super.onResume();
        regHost.observe(getViewLifecycleOwner(), new Observer<Registro>() {
            @Override
            public void onChanged(Registro reg) {
                updateEstimacion(reg);
            }
        });
    }
    private void updateEstimacion(Registro reg){
        if(weeksUntilLastConnection(reg) == 0 && viewModel.getNumOfFotosInRegistro(reg) != 0) {
            Calendar cal = Calendar.getInstance();
            MultipleLinearRegression mlr = MultipleLinearRegression.getINSTANCIA(getContext());
            double[] datosActuales = new double[8];
            datosActuales[0] = reg.getTemp();
            datosActuales[1] = reg.getHrel();
            datosActuales[2] = reg.getPerm25();
            datosActuales[3] = reg.getPer25_33();
            datosActuales[4] = reg.getPer33_50();
            datosActuales[5] = reg.getPer50_70();
            datosActuales[6] = reg.getPer70();
            cal.setTime(reg.getInicioFecha());
            datosActuales[7] = cal.get(Calendar.MONTH);
            int est = mlr.calcularVolumenEstimado(datosActuales);
            estimacion = "" + est;
            QueryPreferencias.guardarEstimacion(getContext(), est);
        }else
            estimacion = QueryPreferencias.cargarEstimacion(getContext());
    }
    private int weeksUntilLastConnection(Registro last){
        Date currentDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(cal.MONDAY);
        int currentADias = (int) TimeUnit.MILLISECONDS.toDays(currentDate.getTime());
        int lastConnectADias = (int) TimeUnit.MILLISECONDS.toDays(last.getInicioFecha().getTime());

        int weeksSinceConnection = (currentADias - lastConnectADias) / 7;
        return weeksSinceConnection;
    }
}