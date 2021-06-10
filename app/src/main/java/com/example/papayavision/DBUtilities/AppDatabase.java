package com.example.papayavision.DBUtilities;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.papayavision.entidades.Registro;
import com.example.papayavision.entidades.RegistroDao;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Registro.class}, version = 1)
@TypeConverters({DateConverters.class})
public abstract class AppDatabase extends RoomDatabase {
    //DAO para los registros
    public abstract RegistroDao registroDao();

    //Instancia unica para la BBDD
    private static volatile AppDatabase INSTANCIA;

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

                }
            }
        }
        return INSTANCIA;
    }
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db){
            super.onCreate(db);
            Date currentDate = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setFirstDayOfWeek(cal.MONDAY);
            int diffDays = cal.getFirstDayOfWeek() - cal.get(Calendar.DAY_OF_WEEK);
            cal.add(Calendar.DAY_OF_WEEK, diffDays);
            // If you want to keep data through app restarts,
            // comment out the following block
            databaseWriteExecutor.execute(() -> {

                RegistroDao registroDao = INSTANCIA.registroDao();

                Registro reg = new Registro(0, cal.getTime());
                registroDao.insertRegistros(reg);
            });
        }
    };
}

