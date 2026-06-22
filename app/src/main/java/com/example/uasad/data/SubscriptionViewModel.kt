package com.example.uasad.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class SubscriptionViewModel(private val repository: SubscriptionRepository) : ViewModel() {

    val allSubscriptions: LiveData<List<Subscription>> = repository.allSubscriptions.asLiveData()
    val upcomingSubscriptions: LiveData<List<Subscription>> = repository.upcomingSubscriptions.asLiveData()

    fun insert(subscription: Subscription) = viewModelScope.launch {
        repository.insert(subscription)
    }

    fun update(subscription: Subscription) = viewModelScope.launch {
        repository.update(subscription)
    }

    private val _selectedCategory = MutableLiveData<SubscriptionCategory>()
    val selectedCategory: LiveData<SubscriptionCategory> = _selectedCategory

    val subscriptionsByCategory: LiveData<List<Subscription>> = selectedCategory.switchMap { category ->
        repository.getByCategory(category).asLiveData()
    }

    fun setCategory(category: SubscriptionCategory) {
        _selectedCategory.value = category
    }

    fun delete(subscription: Subscription) = viewModelScope.launch {
        repository.delete(subscription)
    }

    fun getById(id: Int): LiveData<Subscription> {
        val subscriptionLiveData = MutableLiveData<Subscription>()
        viewModelScope.launch {
            repository.getById(id)?.let {
                subscriptionLiveData.value = it
            }
        }
        return subscriptionLiveData
    }

    fun getTotalMonthlySpending(): Double {
        var total = 0.0

        allSubscriptions.value?.forEach { subscription ->
            total += when (subscription.cycle) {
                SubscriptionCycle.WEEKLY -> subscription.price * 4
                SubscriptionCycle.MONTHLY -> subscription.price
                SubscriptionCycle.YEARLY -> subscription.price / 12
            }
        }
        return total
    }
}
