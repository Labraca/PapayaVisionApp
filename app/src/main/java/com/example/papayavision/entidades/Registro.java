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
import javax.validation.constraints.Null;

@Entity(indices = {@Index(value = {"inicioFecha"},
        unique = true)})
@TypeConverters(DateConverters.class)

public class Registro {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "idRegistro")
    private int idRegistro;
    @ColumnInfo(name = "volumen")
    private int volumen = -1;

    @Size(min = 0,max =100)
    @ColumnInfo(name = "hrel")
    private float hrel= -1;

    @Size(min = 0,max =100)
    @ColumnInfo(name = "perInmaduras")
    private float perInmaduras=0;

    @Size(min = 0,max =100)
    @ColumnInfo(name = "perEnviables")
    private float perEnviables=0;

    @Size(min = 0,max =100)
    @ColumnInfo(name = "perMuyMaduras")
    private float perMuyMaduras = 0;

    @ColumnInfo(name = "temp")
    private float temp = -1;

    public int getVolumen() {
        return volumen;
    }

    public void setVolumen(int volumen) {
        this.volumen = volumen;
    }

    public float getHrel() {
        return hrel;
    }

    public void setHrel(float hrel) {
        this.hrel = hrel;
    }

    public float getPerInmaduras() {
        return perInmaduras;
    }

    public void setPerInmaduras(float perInmaduras) {
        this.perInmaduras = perInmaduras;
    }

    public float getPerEnviables() {
        return perEnviables;
    }

    public void setPerEnviables(float perEnviables) {
        this.perEnviables = perEnviables;
    }

    public float getPerMuyMaduras() {
        return perMuyMaduras;
    }

    public void setPerMuyMaduras(float perMuyMaduras) {
        this.perMuyMaduras = perMuyMaduras;
    }

    public float getTemp() {
        return temp;
    }

    public void setTemp(float temp) {
        this.temp = temp;
    }

    public Date getFinFecha() {
        return finFecha;
    }

    public void setFinFecha(Date finFecha) {
        this.finFecha = finFecha;
    }



    public int getIdRegistro() {
        return idRegistro;
    }

    public void setIdRegistro(int idRegistro) {
        this.idRegistro = idRegistro;
    }

    @NotNull
    @ColumnInfo(name = "inicioFecha")
    private Date inicioFecha;
    @NotNull
    @ColumnInfo(name = "finFecha")
    private Date finFecha;
    public Date getInicioFecha() {
        return inicioFecha;
    }

    public void setInicioFecha(Date inicioFecha) {
        this.inicioFecha = inicioFecha;
    }

    public Registro(){};



    public Registro(int volumen, Date inicioFecha){
        this.volumen=volumen;
        this.inicioFecha=inicioFecha;
        Calendar cal = Calendar.getInstance();
        cal.setTime(inicioFecha);
        cal.add(Calendar.DAY_OF_WEEK,6);
        this.finFecha = cal.getTime();
    }
    public Registro(int volumen,Date inicioFecha,Date finFecha){
        this.volumen = volumen;
        this.inicioFecha = inicioFecha;
        this.finFecha = finFecha;
    }
    public Registro(int volumen,Date inicioFecha,Date finFecha,float[] medias){
        this.volumen = volumen;
        this.inicioFecha = inicioFecha;
        this.finFecha = finFecha;
        this.temp = medias[0];
        this.hrel = medias[1];
    }

}
