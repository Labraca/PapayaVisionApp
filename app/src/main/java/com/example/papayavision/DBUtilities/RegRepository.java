package com.example.papayavision.DBUtilities;

import android.app.Application;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.papayavision.entidades.Foto;
import com.example.papayavision.entidades.Registro;
import com.example.papayavision.entidades.RegistroDao;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Math.round;

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

    public void insertFoto(Foto foto, LifecycleOwner owner){
        updatePercents(foto,owner);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            regDao.insertFoto(foto);
        });

    }
    public void updatePercents(Foto foto, LifecycleOwner owner){
        int idReg = foto.getRegistroId();
        regDao.getRegById(idReg).observe(owner, new Observer<Registro>() {
            @Override
            public void onChanged(Registro registro) {
                getFotos(registro,owner,foto);
            }
        });
    }

    private void getFotos(Registro reg,LifecycleOwner owner,Foto foto) {

        try {
            regDao.getFotosOfReg(reg.getIdRegistro()).observe(owner, new Observer<List<Foto>>() {
                @Override
                public void onChanged(List<Foto> fotos) {
                    int nFotos = fotos.size();
                    float perInmaduras = reg.getPerInmaduras();
                    float perEnviables = reg.getPerEnviables();
                    float perMaduras = reg.getPerMuyMaduras();

                    float newInmaduras = perInmaduras + (foto.getPerInmadura()-perInmaduras)/(nFotos +1);
                    float newEnviables = perEnviables + (foto.getPerEnvio()-perEnviables)/(nFotos +1);
                    float newMaduras = perMaduras + (foto.getPerMadura()-perMaduras)/(nFotos +1);

                    reg.setPerInmaduras(newInmaduras);
                    reg.setPerEnviables(newEnviables);
                    reg.setPerMuyMaduras(newMaduras);

                    update(reg);
                }
            });
        }catch (Exception e){

        }

    }

    public void insert(Registro reg) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            regDao.insertRegistros(reg);
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
