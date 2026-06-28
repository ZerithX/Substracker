package com.example.uasad.data

import kotlinx.coroutines.flow.Flow

class SubscriptionRepository(private val subscriptionDao: SubscriptionDao) {

    val allSubscriptions: Flow<List<Subscription>> = subscriptionDao.getAll()
    val upcomingSubscriptions: Flow<List<Subscription>> = subscriptionDao.getUpcoming()

    suspend fun insert(subscription: Subscription): Long {
        return subscriptionDao.insert(subscription)
    }

    suspend fun update(subscription: Subscription) {
        subscriptionDao.update(subscription)
    }

    suspend fun delete(subscription: Subscription) {
        subscriptionDao.delete(subscription)
    }

    suspend fun getById(id: Int): Subscription? {
        return subscriptionDao.getById(id)
    }
    
    fun getByCategory(category: SubscriptionCategory): Flow<List<Subscription>> {
        return subscriptionDao.getByCategory(category)
    }
}
