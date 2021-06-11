package com.example.papayavision.DBUtilities;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.papayavision.entidades.Registro;
import com.example.papayavision.entidades.RegistroDao;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class RegRepository {

    private RegistroDao regDao;
    private LiveData<List<Registro>> allRegSem;

    // Note that in order to unit test the WordRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    public RegRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        regDao = db.registroDao();
        allRegSem = regDao.getAllRegistrosLive();
    }

    public LiveData<List<Registro>> getAllRegSem() {
        return allRegSem;
    }

    public void insert(Registro word) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            regDao.insertRegistros(word);
        });
    }
    public void update(Registro reg){
        AppDatabase.databaseWriteExecutor.execute(() -> {
            regDao.updateRegistros(reg);
        });
    }
    public LiveData<Registro> getLast() {
        return regDao.getLastRegistro();
    }

    public LiveData<Registro> getRegById(int id){
        return regDao.getRegById(id);
    }
}
