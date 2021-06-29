package com.example.papayavision.entidades;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Transaction;

@Dao
public interface FotosDao {

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertFotos(Foto foto);
}
