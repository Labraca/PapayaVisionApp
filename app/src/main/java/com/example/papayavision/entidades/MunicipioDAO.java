package com.example.papayavision.entidades;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MunicipioDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertAll(List<Municipio> municipioList);
    @Query("SELECT * FROM MUNICIPIO")
    public LiveData<Municipio> getAll();
}
