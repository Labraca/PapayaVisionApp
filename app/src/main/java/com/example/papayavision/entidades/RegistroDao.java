package com.example.papayavision.entidades;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface RegistroDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertRegistros(Registro reg);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertFoto(Foto foto);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertFotos(List<Foto> fotos);

    @Update
    public void updateRegistros(Registro reg);

    @Query("SELECT * FROM REGISTRO")
    public List<Registro> getAllRegistros();
    @Query("SELECT * FROM REGISTRO ORDER BY IDREGISTRO DESC")
    public LiveData<List<Registro>> getAllRegistrosLive();
    @Query("SELECT * FROM REGISTRO ORDER BY IDREGISTRO DESC LIMIT 1")
    public LiveData<Registro> getLastRegistro();
    @Query("SELECT * FROM REGISTRO ORDER BY IDREGISTRO DESC LIMIT 1")
    public Registro getLastRegistroNow();
    @Query("DELETE FROM REGISTRO")
    public void deleteAll();
    @Query("SELECT * FROM REGISTRO WHERE idRegistro = :idReg")
    public LiveData<Registro> getRegById(int idReg);
    @Transaction
    @Query("SELECT * FROM FOTO WHERE registroId = :idRegistro")
    public LiveData<List<Foto>> getFotosOfReg(int idRegistro);
    @Query("SELECT * FROM REGISTRO WHERE idRegistro = :idReg")
    public Registro getRegByIdNow(int idReg);
    @Transaction
    @Query("SELECT * FROM FOTO WHERE registroId = :idRegistro")
    public List<Foto> getFotosOfRegNow(int idRegistro);


}
