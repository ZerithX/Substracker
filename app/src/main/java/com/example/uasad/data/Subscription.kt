package com.example.uasad.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SubscriptionCategory (val value: String){
    WEEKLY ("Weekly"),
    MONTHLY ("Monthly"),
    YERALY ("Yearly")
}

enum class SubscriptionCycle ( val value: String){
    ENTERTAINMENT ("Entertainment"),
    PRODUCTIVITY ("Productivity"),
    EDUCATION ("Education"),
    GAMING ("Gaming"),
    CLOUDSTORAGE ("Cloud Storage"),
    OTHER ("Other")
}

@Entity (tableName = "subscription")
data class Subscription (
    @PrimaryKey (autoGenerate = true) val id: Int = 0,
    @ColumnInfo (name = "name") val name: String = "",
    @ColumnInfo (name = "price") val price: Int = 0,
    @ColumnInfo (name = "category") val category: SubscriptionCategory,
    @ColumnInfo (name = "cycle") val cycle: SubscriptionCycle,
    @ColumnInfo (name = "start_date") val startDate: String = "",
    @ColumnInfo (name = "next_billing") val nextBilling: String = "",
    @ColumnInfo (name = "reminder_enabled") val reminderEnabled: Boolean = false,
    @ColumnInfo (name = "notes") val notes: String = "",
    @ColumnInfo (name = "created_at") val createdAt: String = ""
)