package com.example.papayavision.entidades;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.papayavision.DBUtilities.RegRepository;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RegistroViewModel extends AndroidViewModel {

    private RegRepository repository;
    private final MutableLiveData<Registro> selectedItem = new MutableLiveData<Registro>();
    private final LiveData<List<Registro>> allRegSem;

    public RegistroViewModel(@NonNull @NotNull Application application) {
        super(application);
        repository = new RegRepository(application);
        allRegSem = repository.getAllRegSem();
    }

    public LiveData<List<Registro>> getAllRegSem() {
        return allRegSem;
    }

    public void insert(Registro reg){repository.insert(reg);}

    public void insertFoto(Foto foto, LifecycleOwner owner){repository.insertFoto(foto,owner);}

    public void update(Registro reg){repository.update(reg);}

    public void selectItem(Registro reg) {
        selectedItem.setValue(reg);
    }
    public int getNumOfFotosInRegistro(Registro reg){
        return repository.getAllFotosFromReg(reg).size();
    }
    public LiveData<Registro> getSelectedItem() {
        return selectedItem;
    }
}
