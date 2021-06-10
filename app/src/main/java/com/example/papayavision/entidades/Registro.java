package com.example.papayavision.entidades;


import androidx.annotation.Size;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.example.papayavision.DBUtilities.DateConverters;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

import javax.validation.constraints.NotNull;

@Entity(indices = {@Index(value = {"inicioFecha"},
        unique = true)})
@TypeConverters(DateConverters.class)

public class Registro {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "volumen")
    private int volumen;

    @Size(min = 0,max =100)
    @ColumnInfo(name = "hrel")
    private int hrel;
    @Size(min = 0,max =100)
    @ColumnInfo(name = "perMuyInmaduras")
    private int perMuyInmaduras=0;
    @Size(min = 0,max =100)
    @ColumnInfo(name = "perInmaduras")
    private int perInmaduras=0;
    @Size(min = 0,max =100)
    @ColumnInfo(name = "perEnviables")
    private int perEnviables=0;
    @Size(min = 0,max =100)
    @ColumnInfo(name = "perMuyMaduras")
    private int perMuyMaduras = 0;
    @ColumnInfo(name = "temp")
    private int temp;
    @NotNull
    @ColumnInfo(name = "inicioFecha")
    private Date inicioFecha;

    public int getVolumen() {
        return volumen;
    }

    public void setVolumen(int volumen) {
        this.volumen = volumen;
    }

    public int getHrel() {
        return hrel;
    }

    public void setHrel(int hrel) {
        this.hrel = hrel;
    }

    public int getPerMuyInmaduras() {
        return perMuyInmaduras;
    }

    public void setPerMuyInmaduras(int perMuyInmaduras) {
        this.perMuyInmaduras = perMuyInmaduras;
    }

    public int getPerInmaduras() {
        return perInmaduras;
    }

    public void setPerInmaduras(int perInmaduras) {
        this.perInmaduras = perInmaduras;
    }

    public int getPerEnviables() {
        return perEnviables;
    }

    public void setPerEnviables(int perEnviables) {
        this.perEnviables = perEnviables;
    }

    public int getPerMuyMaduras() {
        return perMuyMaduras;
    }

    public void setPerMuyMaduras(int perMuyMaduras) {
        this.perMuyMaduras = perMuyMaduras;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public Registro(){};

    public Date getInicioFecha() {
        return inicioFecha;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setInicioFecha(Date inicioFecha) {
        this.inicioFecha = inicioFecha;
    }

    public Registro(int volumen, Date inicioFecha){
        this.volumen=volumen;
        this.inicioFecha=inicioFecha;
        //this.hrel= API humedad
        //this.temp= API temperatura
    }

}
