package com.example.uasad.data

import android.content.Context
import androidx.room.Room
import com.example.uasad.data.SubsTrackerDatabase

object DatabaseBuilder {
    @Volatile
    private var INSTANCE: SubsTrackerDatabase? = null

    fun getInstance(context: Context): SubsTrackerDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                SubsTrackerDatabase::class.java,
                "subs_tracker_db"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}