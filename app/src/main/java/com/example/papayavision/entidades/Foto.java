package com.example.papayavision.entidades;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Foto {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "pathImage")
    private String pathImage;
    private float perm25=0;
    private float per25_33=0;
    private float per33_50=0;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPathImage() {
        return pathImage;
    }

    public void setPathImage(String pathImage) {
        this.pathImage = pathImage;
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

    public int getRegistroId() {
        return registroId;
    }

    public void setRegistroId(int registroId) {
        this.registroId = registroId;
    }

    private float per50_70=0;
    private float per70=0;

    private int registroId=0;



}
