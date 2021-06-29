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

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.papayavision.entidades.Registro;
import com.example.papayavision.entidades.RegistroViewModel;

import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegDatosFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegDatosFragment extends Fragment {
    private RegistroViewModel viewModel;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private EditText volReg;
    private LiveData<Registro> regHost;
    private ConstraintLayout fotosContainer;
    private String SUFFIX = "kg";
    public RegDatosFragment() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RegDatosFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RegDatosFragment newInstance(String param1, String param2) {
        RegDatosFragment fragment = new RegDatosFragment();
        /*Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);*/
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        
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
        regHost = viewModel.getSelectedItem();
        regHost.observe(getViewLifecycleOwner(), new Observer<Registro>() {
            @Override
            public void onChanged(Registro registro) {
                volReg.setText(registro.getVolumen()+SUFFIX);
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
               String w = s.toString().substring(0,s.length()-2);
                if(regex.matcher(w).matches())
                    previousText = w;
                else
                    s.replace(0,s.length()-2,previousText);
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



        //volEstimado = findViewById(R.id.volEstimadoLabel);
        //tempActual = findViewById(R.id.tempActual);
        // tempPrxSem = findViewById(R.id.tempPrxSem);
        // hrelActual = findViewById(R.id.hrelActual);
        // hrelPrxSem = findViewById(R.id.hrelPrxSem);

    }
}