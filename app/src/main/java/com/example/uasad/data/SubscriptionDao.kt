package com.example.uasad.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao{
    // ABORT untuk konflik data ketika duplikat
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(subscription: Subscription): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(subscriptions: List<Subscription>)

    @Update suspend fun update(subscription: Subscription)

    @Delete suspend fun delete(subscription: Subscription)

    @Query("SELECT * FROM subscription")
    fun getAll(): Flow<List<Subscription>>

    @Query("SELECT * FROM subscription WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Subscription?

    @Query("SELECT * FROM subscription WHERE category = :category")
    fun getByCategory(category: SubscriptionCategory): Flow<List<Subscription>>

    @Query("SELECT * FROM subscription ORDER BY next_billing ASC LIMIT 5")
    fun getUpcoming(): Flow<List<Subscription>>
}
