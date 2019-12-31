package com.aftershoot.declutter.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Image::class], version = 1)
@TypeConverters(Converters::class)
abstract class AfterShootDatabase : RoomDatabase() {

    abstract fun getDao(): AfterShootDao

    companion object {

        private var database: AfterShootDatabase? = null

        // Singleton database object, don't create this unnecessarily
        fun getDatabase(context: Context): AfterShootDatabase? {

            database ?: kotlin.run {
                database = Room.databaseBuilder(context, AfterShootDatabase::class.java, "aftershootdb")
                        .fallbackToDestructiveMigration()
                        .build()
            }

            return database
        }
    }

}