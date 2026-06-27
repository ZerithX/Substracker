package com.example.uasad.home

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.uasad.data.Subscription
import com.example.uasad.data.SubscriptionCategory
import com.example.uasad.data.SubscriptionCycle
import com.example.uasad.data.getBrandColor
import com.example.uasad.databinding.ItemSubscriptionListBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class UpcomingAdapter(
    private val onItemClick: (Subscription) -> Unit
) : ListAdapter<Subscription, UpcomingAdapter.UpcomingViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UpcomingViewHolder {
        val binding = ItemSubscriptionListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UpcomingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UpcomingViewHolder, position: Int) {
        val subscription = getItem(position)
        holder.bind(subscription)
    }

    inner class UpcomingViewHolder(private val binding: ItemSubscriptionListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(adapterPosition))
                }
            }
        }

        fun bind(subscription: Subscription) {
            val context = binding.root.context
            
            // Inisial avatar
            val initial = subscription.name.firstOrNull()?.uppercase() ?: "S"
            binding.tvAvatar.text = initial

            // Set warna background avatar dinamis berdasarkan nama brand atau kategori
            val brandColor = subscription.getBrandColor()
            val drawable = binding.tvAvatar.background as? GradientDrawable
            drawable?.setColor(brandColor)

            // Nama layanan
            binding.tvName.text = subscription.name

            // Harga terformat Rupiah
            binding.tvPrice.text = formatRupiah(subscription.price)

            // Siklus label
            binding.tvCycleLabel.text = when (subscription.cycle) {
                SubscriptionCycle.WEEKLY -> "Mingguan"
                SubscriptionCycle.MONTHLY -> "Bulanan"
                SubscriptionCycle.YEARLY -> "Tahunan"
            }
            binding.tvCycleSuffix.text = when (subscription.cycle) {
                SubscriptionCycle.WEEKLY -> "/mgg"
                SubscriptionCycle.MONTHLY -> "/bln"
                SubscriptionCycle.YEARLY -> "/thn"
            }

            // Kategori badge
            binding.tvCategory.text = subscription.category.value
            styleCategoryBadge(binding.tvCategory, subscription.category)

            // Tanggal billing berikutnya
            binding.tvNextBilling.text = formatNextBilling(subscription.nextBilling)

            // Hitung sisa hari untuk warning badge dan border merah
            val daysLeft = getDaysLeft(subscription.nextBilling)
            if (daysLeft in 0..3) {
                binding.tvWarningBadge.visibility = View.VISIBLE
                binding.tvWarningBadge.text = when (daysLeft) {
                    0L -> "Hari ini!"
                    1L -> "Besok!"
                    else -> "$daysLeft hari lagi"
                }
                // Tampilkan stroke merah pada card
                binding.cvSubscription.strokeWidth = dpToPx(1.5f, context)
            } else if (daysLeft < 0 && daysLeft > -999) {
                binding.tvWarningBadge.visibility = View.VISIBLE
                binding.tvWarningBadge.text = "Terlewat!"
                binding.cvSubscription.strokeWidth = dpToPx(1.5f, context)
            } else {
                binding.tvWarningBadge.visibility = View.GONE
                binding.cvSubscription.strokeWidth = 0
            }
        }
    }

    private fun getDaysLeft(nextBilling: String): Long {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(nextBilling)
            if (date != null) {
                val today = Calendar.getInstance()
                today.set(Calendar.HOUR_OF_DAY, 0)
                today.set(Calendar.MINUTE, 0)
                today.set(Calendar.SECOND, 0)
                today.set(Calendar.MILLISECOND, 0)

                val diff = date.time - today.timeInMillis
                return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return -999L
    }

    private fun formatNextBilling(dateStr: String): String {
        try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = parser.parse(dateStr)
            if (date != null) {
                val formatter = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
                return "Next billing: ${formatter.format(date)}"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return dateStr
    }

    private fun formatRupiah(price: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(price).replace("Rp", "Rp ")
    }

    private fun styleCategoryBadge(textView: TextView, category: SubscriptionCategory) {
        val (bgColor, textColor) = when (category) {
            SubscriptionCategory.ENTERTAINMENT -> Pair("#FCE4EC", "#C2185B")
            SubscriptionCategory.PRODUCTIVITY -> Pair("#E8EAF6", "#3F51B5")
            SubscriptionCategory.CLOUDSTORAGE -> Pair("#E0F7FA", "#00838F")
            SubscriptionCategory.EDUCATION -> Pair("#FFF3E0", "#E65100")
            SubscriptionCategory.GAMING -> Pair("#F3E5F5", "#7B1FA2")
            else -> Pair("#F5F5F5", "#616161")
        }
        textView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(bgColor))
        textView.setTextColor(Color.parseColor(textColor))
    }

    private fun dpToPx(dp: Float, context: android.content.Context): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Subscription>() {
            override fun areItemsTheSame(oldItem: Subscription, newItem: Subscription): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Subscription, newItem: Subscription): Boolean {
                return oldItem == newItem
            }
        }
    }
}
