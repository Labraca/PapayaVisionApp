package com.example.papayavision.entidades;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface RegistroDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertRegistros(Registro reg);
    @Update
    public void updateRegistros(Registro reg);
    @Query("SELECT * FROM REGISTRO")
    public List<Registro> getAllRegistros();
    @Query("SELECT * FROM REGISTRO ORDER BY ID DESC")
    public LiveData<List<Registro>> getAllRegistrosLive();
    @Query("SELECT * FROM REGISTRO ORDER BY ID DESC LIMIT 1")
    public LiveData<Registro> getLastRegistro();
    @Query("DELETE FROM REGISTRO")
    public void deleteAll();
    @Query("SELECT * FROM REGISTRO WHERE id = :idReg")
    public LiveData<Registro> getRegById(int idReg);

}
