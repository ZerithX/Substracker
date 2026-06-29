package com.example.uasad.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Subscription::class], version = 2, exportSchema = false)
abstract class SubsTrackerDatabase : RoomDatabase() {
    abstract fun subscriptionDao(): SubscriptionDao
}