package com.bubu.cycle.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SupportFactory

@Database(entities = [PeriodLog::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun periodDao(): PeriodDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val prefs = SecurePrefs.get(context)
                val passphrase = prefs.getString("db_pass", null) ?: run {
                    val newPass = java.util.UUID.randomUUID().toString()
                    prefs.edit().putString("db_pass", newPass).apply()
                    newPass
                }

                val factory = SupportFactory(passphrase.toByteArray())

                Room.databaseBuilder(context, AppDatabase::class.java, "cycle.db")
                    .openHelperFactory(factory)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
