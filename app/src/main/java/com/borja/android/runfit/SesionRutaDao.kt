package com.borja.android.runfit

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SesionRutaDao {
    @Insert
    suspend fun insertSesionRuta(sesionRuta: SesionRuta): Long

    @Query("SELECT * FROM sesiones_ruta")
    suspend fun getAllSesionesRuta(): List<SesionRuta>
}