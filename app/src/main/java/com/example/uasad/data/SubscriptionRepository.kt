package com.example.uasad.data

import kotlinx.coroutines.flow.Flow

class SubscriptionRepository(private val subscriptionDao: SubscriptionDao) {

    val allSubscriptions: Flow<List<Subscription>> = subscriptionDao.getAll()
    val upcomingSubscriptions: Flow<List<Subscription>> = subscriptionDao.getUpcoming()

    // 'suspend' = operasi ini tidak boleh di Main Thread
    suspend fun insert(subscription: Subscription) = subscriptionDao.insert(subscription)
    suspend fun update(subscription: Subscription) = subscriptionDao.update(subscription)
    suspend fun delete(subscription: Subscription) = subscriptionDao.delete(subscription)

    suspend fun getById(id: Int) = subscriptionDao.getById(id)
}
