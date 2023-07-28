package com.borja.android.runfit

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sesiones_ruta")
data class SesionRuta(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    val distancia: Float,
    val velocidadPromedio: Double,
    val tiempo: Long,
    val calorias: Double,
    val fecha: String,
    val hora: String
)
