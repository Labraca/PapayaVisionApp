package com.example.papayavision.entidades;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.validation.constraints.NotNull;

@Entity()
public class Municipio {
    @NotNull
    @ColumnInfo(name = "Municipio")
    private String municipio;

    @PrimaryKey
    @ColumnInfo(name = "CodMun")
    private @NonNull String codMunicipio;

    public String getCodMunicipio() {
        return codMunicipio;
    }

    public void setCodMunicipio(String codMunicipio) {
        this.codMunicipio = codMunicipio;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }



    public Municipio(){}
    public Municipio(String municipio,String codMunicipio){
        this.municipio=municipio;
        this.codMunicipio=codMunicipio;
    }

}
