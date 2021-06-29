package com.example.papayavision.DBUtilities;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.papayavision.entidades.Foto;
import com.example.papayavision.entidades.Municipio;
import com.example.papayavision.entidades.MunicipioDAO;
import com.example.papayavision.entidades.Registro;
import com.example.papayavision.entidades.RegistroDao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Registro.class, Municipio.class, Foto.class}, version = 1)
@TypeConverters({DateConverters.class})
public abstract class AppDatabase extends RoomDatabase {
    //DAO para los registros
    public abstract RegistroDao registroDao();
    public abstract MunicipioDAO municipioDAO();
    //Instancia unica para la BBDD
    private static volatile AppDatabase INSTANCIA;
    private static AssetManager am;
    //Servicio multihilo para crear hilos segun demanda
    static final ExecutorService databaseWriteExecutor =
            Executors.newCachedThreadPool();

    // Acceso a la Instancia
    static AppDatabase getDatabase(final Context context) {
        if (INSTANCIA == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCIA == null) {
                    INSTANCIA = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "Registros_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                    am = context.getAssets();
                }
            }
        }
        return INSTANCIA;
    }
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db){
            super.onCreate(db);
            Calendar cal = Calendar.getInstance();
            cal.setFirstDayOfWeek(cal.MONDAY);
            int diffDays = cal.getFirstDayOfWeek() - cal.get(Calendar.DAY_OF_WEEK);
            cal.add(Calendar.DAY_OF_WEEK, diffDays);


            // If you want to keep data through app restarts,
            // comment out the following block
            databaseWriteExecutor.execute(() -> {

                RegistroDao registroDao = INSTANCIA.registroDao();
                MunicipioDAO municipioDAO = INSTANCIA.municipioDAO();
                List<Municipio> municipios= null;
                try {
                    municipios = parseMuniToList();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                municipioDAO.insertAll(municipios);
                Registro reg = new Registro(0, cal.getTime());
                registroDao.insertRegistros(reg);
            });
        }
    };
    private static List<Municipio> parseMuniToList() throws IOException {

        InputStream f = am.open("Municipio.txt");
        ArrayList<Municipio> municipios = new ArrayList<Municipio>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(f))) {

            String line = null;
            while((line = br.readLine()) != null) {

                String[] codMun = line.split(";");
                Municipio mun = new Municipio(codMun[1],codMun[0]);
                municipios.add(mun);

            }
        } catch (IOException e) {

            e.printStackTrace();

        }
        f.close();
        return municipios;
    }
}

