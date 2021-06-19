package com.example.papayavision.entidades;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.papayavision.DBUtilities.RegRepository;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RegistroViewModel extends AndroidViewModel {

    private RegRepository repository;
    private final MutableLiveData<Registro> selectedItem = new MutableLiveData<Registro>();
    public void selectItem(Registro reg) {
        selectedItem.setValue(reg);
    }
    public LiveData<Registro> getSelectedItem() {
        return selectedItem;
    }
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

    public void update(Registro reg){repository.update(reg);}

}
