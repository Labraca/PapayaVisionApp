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
    @ColumnInfo(name = "perm25")
    private float perm25=0;
    @Size(min = 0,max =100)
    @ColumnInfo(name = "per25_33")
    private float per25_33=0;
    @Size(min = 0,max =100)
    @ColumnInfo(name = "per33_55")
    private float per33_50=0;
    @Size(min = 0,max =100)
    @ColumnInfo(name = "per50_70")
    private float per50_70=0;
    @Size(min = 0,max =100)
    @ColumnInfo(name = "per70")
    private float per70=0;

    @ColumnInfo(name = "temp")
    private float temp = -274;

    public int getIdRegistro() {
        return idRegistro;
    }

    public void setIdRegistro(int idRegistro) {
        this.idRegistro = idRegistro;
    }

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

    public float getPerm25() {
        return perm25;
    }

    public void setPerm25(float perm25) {
        this.perm25 = perm25;
    }

    public float getPer25_33() {
        return per25_33;
    }

    public void setPer25_33(float per25_33) {
        this.per25_33 = per25_33;
    }

    public float getPer33_50() {
        return per33_50;
    }

    public void setPer33_50(float per33_50) {
        this.per33_50 = per33_50;
    }

    public float getPer50_70() {
        return per50_70;
    }

    public void setPer50_70(float per50_70) {
        this.per50_70 = per50_70;
    }

    public float getPer70() {
        return per70;
    }

    public void setPer70(float per70) {
        this.per70 = per70;
    }

    public float getTemp() {
        return temp;
    }

    public void setTemp(float temp) {
        this.temp = temp;
    }

    public Date getInicioFecha() {
        return inicioFecha;
    }

    public void setInicioFecha(Date inicioFecha) {
        this.inicioFecha = inicioFecha;
    }

    public Date getFinFecha() {
        return finFecha;
    }

    public void setFinFecha(Date finFecha) {
        this.finFecha = finFecha;
    }

    @NotNull
    @ColumnInfo(name = "inicioFecha")
    private Date inicioFecha;
    @NotNull
    @ColumnInfo(name = "finFecha")
    private Date finFecha;


    public Registro(){}



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
