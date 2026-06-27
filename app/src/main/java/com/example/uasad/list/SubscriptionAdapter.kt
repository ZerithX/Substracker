package com.example.uasad.list

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.uasad.data.Subscription
import com.example.uasad.data.SubscriptionCategory
import com.example.uasad.data.SubscriptionCycle
import com.example.uasad.data.getBrandColor
import com.example.uasad.databinding.ItemSubscriptionGridBinding
import com.example.uasad.databinding.ItemSubscriptionListBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class SubscriptionAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<Subscription> = emptyList()
    var isGridView: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private var onItemClickListener: ((Subscription) -> Unit)? = null

    fun submitList(newList: List<Subscription>) {
        items = newList
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: (Subscription) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemViewType(position: Int): Int {
        return if (isGridView) VIEW_TYPE_GRID else VIEW_TYPE_LIST
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_GRID) {
            val binding = ItemSubscriptionGridBinding.inflate(inflater, parent, false)
            GridViewHolder(binding)
        } else {
            val binding = ItemSubscriptionListBinding.inflate(inflater, parent, false)
            ListViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val subscription = items[position]
        if (holder is GridViewHolder) {
            holder.bind(subscription)
        } else if (holder is ListViewHolder) {
            holder.bind(subscription)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ListViewHolder(private val binding: ItemSubscriptionListBinding) :
        RecyclerView.ViewHolder(binding.root) {

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
            binding.tvNextBilling.text = formatNextBilling(subscription.nextBilling, false)

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

            // Navigasi detail
            binding.root.setOnClickListener {
                onItemClickListener?.invoke(subscription)
            }
        }
    }

    inner class GridViewHolder(private val binding: ItemSubscriptionGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(subscription: Subscription) {
            val context = binding.root.context

            // Inisial avatar
            val initial = subscription.name.firstOrNull()?.uppercase() ?: "S"
            binding.tvGridAvatar.text = initial

            // Set warna background header dinamis
            val brandColor = subscription.getBrandColor()
            binding.flHeaderBg.setBackgroundColor(brandColor)

            // Nama layanan
            binding.tvName.text = subscription.name

            // Harga
            binding.tvPrice.text = formatRupiah(subscription.price)

            // Kategori badge
            binding.tvCategory.text = subscription.category.value
            styleCategoryBadge(binding.tvCategory, subscription.category)

            // Tanggal billing berikutnya (format ringkas)
            binding.tvNextBilling.text = formatNextBilling(subscription.nextBilling, true)

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

            // Navigasi detail
            binding.root.setOnClickListener {
                onItemClickListener?.invoke(subscription)
            }
        }
    }

    // --- Helper Methods ---

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

    private fun formatNextBilling(dateStr: String, isGrid: Boolean): String {
        try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = parser.parse(dateStr)
            if (date != null) {
                return if (isGrid) {
                    val formatter = SimpleDateFormat("d/M/yy", Locale.getDefault())
                    formatter.format(date)
                } else {
                    val formatter = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
                    "Next billing: ${formatter.format(date)}"
                }
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
        private const val VIEW_TYPE_LIST = 1
        private const val VIEW_TYPE_GRID = 2
    }
}
