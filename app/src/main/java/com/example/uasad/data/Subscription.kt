package com.example.uasad.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SubscriptionCycle (val value: String){
    WEEKLY ("Mingguan"),
    MONTHLY ("Bulanan"),
    YEARLY ("Tahunan")
}

enum class SubscriptionCategory ( val value: String){
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
    @ColumnInfo (name = "price") val price: Double = 0.0,
    @ColumnInfo (name = "category") val category: SubscriptionCategory,
    @ColumnInfo (name = "cycle") val cycle: SubscriptionCycle,
    @ColumnInfo (name = "start_date") val startDate: String = "",
    @ColumnInfo (name = "next_billing") val nextBilling: String = "",
    @ColumnInfo (name = "reminder_enabled") val reminderEnabled: Boolean = false,
    @ColumnInfo (name = "notes") val notes: String = "",
    @ColumnInfo (name = "created_at") val createdAt: String = ""
)

fun Subscription.getBrandColor(): Int {
    val name = this.name.lowercase(java.util.Locale.getDefault()).trim()
    return when {
        name.contains("netflix") -> android.graphics.Color.parseColor("#E50914")
        name.contains("spotify") -> android.graphics.Color.parseColor("#1DB954")
        name.contains("youtube") -> android.graphics.Color.parseColor("#FF0000")
        name.contains("notion") -> android.graphics.Color.parseColor("#000000")
        name.contains("github") -> android.graphics.Color.parseColor("#1F2328")
        name.contains("google") -> android.graphics.Color.parseColor("#4285F4")
        else -> {
            // Fallback ke warna kategori
            when (this.category) {
                SubscriptionCategory.ENTERTAINMENT -> android.graphics.Color.parseColor("#E91E63")
                SubscriptionCategory.PRODUCTIVITY -> android.graphics.Color.parseColor("#3F51B5")
                SubscriptionCategory.CLOUDSTORAGE -> android.graphics.Color.parseColor("#00BCD4")
                SubscriptionCategory.EDUCATION -> android.graphics.Color.parseColor("#FF9800")
                SubscriptionCategory.GAMING -> android.graphics.Color.parseColor("#9C27B0")
                else -> android.graphics.Color.parseColor("#757575")
            }
        }
    }
}
