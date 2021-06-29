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
    private float perInmadura=0;
    private float perEnvio=0;
    private float perMadura=0;
    private int registroId=0;

    public float getPerInmadura() {
        return perInmadura;
    }

    public void setPerInmadura(float perInmadura) {
        this.perInmadura = perInmadura;
    }

    public float getPerEnvio() {
        return perEnvio;
    }

    public void setPerEnvio(float perEnvio) {
        this.perEnvio = perEnvio;
    }

    public float getPerMadura() {
        return perMadura;
    }

    public void setPerMadura(float perMadura) {
        this.perMadura = perMadura;
    }


    public int getRegistroId() {
        return registroId;
    }

    public void setRegistroId(int registroId) {
        this.registroId = registroId;
    }

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

}
