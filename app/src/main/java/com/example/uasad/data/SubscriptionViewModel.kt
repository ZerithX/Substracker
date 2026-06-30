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
import kotlinx.coroutines.flow.first

class SubscriptionViewModel(private val repository: SubscriptionRepository) : ViewModel() {

    val allSubscriptions: LiveData<List<Subscription>> = repository.allSubscriptions.asLiveData()
    val upcomingSubscriptions: LiveData<List<Subscription>> = repository.upcomingSubscriptions.asLiveData()

    fun insert(subscription: Subscription, onInserted: (Long) -> Unit = {}) = viewModelScope.launch {
        val id = repository.insert(subscription)
        onInserted(id)
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

    fun checkAndResetPassedSubscriptions(context: android.content.Context) = viewModelScope.launch {
        try {
            val list = repository.allSubscriptions.first()
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val today = java.util.Calendar.getInstance()
            today.set(java.util.Calendar.HOUR_OF_DAY, 0)
            today.set(java.util.Calendar.MINUTE, 0)
            today.set(java.util.Calendar.SECOND, 0)
            today.set(java.util.Calendar.MILLISECOND, 0)

            list.forEach { subscription ->
                try {
                    val billingDate = sdf.parse(subscription.nextBilling)
                    if (billingDate != null) {
                        val calendar = java.util.Calendar.getInstance()
                        calendar.time = billingDate
                        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                        calendar.set(java.util.Calendar.MINUTE, 0)
                        calendar.set(java.util.Calendar.SECOND, 0)
                        calendar.set(java.util.Calendar.MILLISECOND, 0)

                        if (calendar.before(today)) {
                            // Hitung tanggal baru yang >= hari ini
                            while (calendar.before(today)) {
                                when (subscription.cycle) {
                                    SubscriptionCycle.WEEKLY -> calendar.add(java.util.Calendar.WEEK_OF_YEAR, 1)
                                    SubscriptionCycle.MONTHLY -> calendar.add(java.util.Calendar.MONTH, 1)
                                    SubscriptionCycle.YEARLY -> calendar.add(java.util.Calendar.YEAR, 1)
                                }
                            }
                            val newBillingDateStr = sdf.format(calendar.time)
                            val updatedSubscription = subscription.copy(nextBilling = newBillingDateStr)
                            
                            // Update di database
                            repository.update(updatedSubscription)
                            
                            // Jika reminder aktif, jadwalkan ulang alarm
                            if (updatedSubscription.reminderEnabled) {
                                com.example.uasad.utils.AlarmScheduler.scheduleReminder(context, updatedSubscription, 1)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
