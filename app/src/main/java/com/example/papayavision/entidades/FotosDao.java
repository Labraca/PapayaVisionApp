package com.example.papayavision.entidades;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import static android.icu.text.MessagePattern.ArgType.SELECT;

@Dao
public interface FotosDao {

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertFotos(Foto foto);
    @Query("SELECT * FROM FOTO Where registroId = :idRegistro")
    public List<Foto> getAllFromReg(int idRegistro);
}
