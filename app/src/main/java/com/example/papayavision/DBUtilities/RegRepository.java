package com.example.papayavision.DBUtilities;

import android.app.Application;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.papayavision.entidades.Foto;
import com.example.papayavision.entidades.FotosDao;
import com.example.papayavision.entidades.Municipio;
import com.example.papayavision.entidades.MunicipioDAO;
import com.example.papayavision.entidades.Registro;
import com.example.papayavision.entidades.RegistroDao;
import com.example.papayavision.regUtilities.MultipleLinearRegression;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Math.round;

public class RegRepository {

    private RegistroDao regDao;
    private LiveData<List<Registro>> allRegSem;
    private MunicipioDAO munDao;
    private FotosDao fotosDao;
    private WeatherAPIAdapter weatherAPIAdapter;
    // Note that in order to unit test the WordRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    public RegRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        regDao = db.registroDao();
        allRegSem = regDao.getAllRegistrosLive();
        munDao = db.municipioDAO();
        fotosDao = db.fotosDao();
        weatherAPIAdapter = db.wAPIAdapter;
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
                insertFoto(registro,owner,foto);
            }
        });
    }

    private void insertFoto(Registro reg,LifecycleOwner owner,Foto foto) {

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

                    Calendar cal = Calendar.getInstance();
                    update(reg);
                    //calculo la nueva estimacion para esta semana

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

    public Registro getLastNow(){ return  regDao.getLastRegistroNow();}

    public LiveData<Registro> getRegById(int id){
        return regDao.getRegById(id);
    }

    public  LiveData<List<String>> getAllNombreMuni(){ return munDao.getAllNombresMuni();}

    public List<Municipio> getAllMunicipios(){
        try {
            return AppDatabase.databaseWriteExecutor.submit(new GetMunicipio(munDao)).get();
        }catch (Exception e){
            return null;
        }

    }
    public List<Foto> getAllFotosFromReg(Registro reg){
        try {
            return AppDatabase.databaseWriteExecutor.submit(new GetFotos(fotosDao,reg)).get();
        }catch (Exception e){
            return null;
        }
    }

    public void updateMedias(LifecycleOwner owner,String codMun){
        getLast().observe(owner, new Observer<Registro>() {
            @Override
            public void onChanged(Registro registro) {
                if(registro.getHrel() < 0)
                weatherAPIAdapter.updateMediasRegistro(codMun,registro);
            }
        });
    }
    private static class GetMunicipio implements Callable<List<Municipio>> {
        private MunicipioDAO munDao;
        public GetMunicipio(MunicipioDAO munDao) {
            this.munDao = munDao;
        }

        @Override
        public List<Municipio> call() throws Exception {
            return munDao.getAll();
        }

    }
    private static class GetFotos implements Callable<List<Foto>> {
        private FotosDao fotoDao;
        private Registro reg;
        public GetFotos(FotosDao fotoDao,Registro reg) {
            this.reg = reg;
            this.fotoDao = fotoDao;
        }

        @Override
        public List<Foto> call() throws Exception {
            return fotoDao.getAllFromReg(reg.getIdRegistro());
        }

    }

}
