package com.example.modoguardian.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class EventoDao {

    @Query("SELECT * FROM eventos ORDER BY CAST(id AS INTEGER) ASC")
    abstract suspend fun obtenerTodos(): List<EventoEntity>

    @Query("DELETE FROM eventos")
    abstract suspend fun borrarTodos()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertarTodos(eventos: List<EventoEntity>)

    // Reemplaza todo el contenido en una sola transacción, para no dejar la copia a medias
    @Transaction
    open suspend fun reemplazarTodos(eventos: List<EventoEntity>) {
        borrarTodos()
        insertarTodos(eventos)
    }
}