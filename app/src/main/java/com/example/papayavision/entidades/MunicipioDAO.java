package com.example.papayavision.entidades;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

import java.util.List;

@Dao
public interface MunicipioDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertAll(List<Municipio> municipioList);
}
