package com.example.papayavision.entidades;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class RegistroConFotos {
    @Embedded public Registro registro;
    @Relation(
            parentColumn = "idRegistro",
            entityColumn = "registroId"
    )
    public List<Foto> fotos;
}
