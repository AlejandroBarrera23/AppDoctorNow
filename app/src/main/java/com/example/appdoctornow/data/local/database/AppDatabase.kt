package com.example.appdoctornow.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.appdoctornow.data.local.dao.UsuarioDao
import com.example.appdoctornow.data.local.dao.MedicoDao
import com.example.appdoctornow.data.local.dao.CitaDao
import com.example.appdoctornow.model.Usuario
import com.example.appdoctornow.model.Medico
import com.example.appdoctornow.model.Cita

@Database(
    entities = [Usuario::class, Medico::class, Cita::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun medicoDao(): MedicoDao
    abstract fun citaDao(): CitaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mi_app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}