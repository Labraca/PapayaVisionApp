package com.example.papayavision.entidades;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Entity()
public class Municipio {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "Municipio")
    private String municipio;
    @ColumnInfo(name = "CodEstacion")
    private String codEstacion;

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public String getCodEstacion() {
        return codEstacion;
    }

    public void setCodEstacion(String codEstacion) {
        this.codEstacion = codEstacion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public Municipio(){}
    public Municipio(String municipio,String codEstacion){
        this.municipio=municipio;
        this.codEstacion=codEstacion;
    }

}
