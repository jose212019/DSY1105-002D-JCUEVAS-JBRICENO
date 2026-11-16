package com.example.modoguardian.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Base de datos local de la app con la copia de eventos y el registro de acciones
@Database(entities = [EventoEntity::class, AccionEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun eventoDao(): EventoDao
    abstract fun accionDao(): AccionDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase {
            return instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "modoguardian.db"
                ).build().also { instancia = it }
            }
        }
    }
}