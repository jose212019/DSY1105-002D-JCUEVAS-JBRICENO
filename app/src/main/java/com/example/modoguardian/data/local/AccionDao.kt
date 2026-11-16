package com.example.modoguardian.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AccionDao {

    @Insert
    suspend fun insertar(accion: AccionEntity)

    @Query("SELECT * FROM acciones ORDER BY fecha DESC")
    fun observarTodas(): Flow<List<AccionEntity>>
}