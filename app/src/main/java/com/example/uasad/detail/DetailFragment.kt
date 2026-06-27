package com.example.uasad.detail

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.uasad.R
import com.example.uasad.data.DatabaseBuilder
import com.example.uasad.data.Subscription
import com.example.uasad.data.getBrandColor
import com.example.uasad.data.SubscriptionRepository
import com.example.uasad.data.SubscriptionViewModel
import com.example.uasad.data.SubscriptionViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.snackbar.Snackbar
import android.content.res.ColorStateList
import android.graphics.Color
import com.example.uasad.data.SubscriptionCategory
import com.example.uasad.utils.AlarmUtils
import java.text.NumberFormat
import java.util.Locale

class DetailFragment : Fragment() {

    private lateinit var viewModel: SubscriptionViewModel
    private var subscriptionId: Int = -1
    private var currentSubscription: Subscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            subscriptionId = it.getInt("subscriptionId", -1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        val database = DatabaseBuilder.getInstance(requireContext())
        val repository = SubscriptionRepository(database.subscriptionDao())
        val factory = SubscriptionViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(SubscriptionViewModel::class.java)

        val btnBack = view.findViewById<ImageView>(R.id.btn_back)
        val btnDelete = view.findViewById<MaterialButton>(R.id.btn_delete)
        val btnEdit = view.findViewById<MaterialButton>(R.id.btn_edit)

        val tvAvatar = view.findViewById<TextView>(R.id.tv_avatar)
        val tvName = view.findViewById<TextView>(R.id.tv_detail_name)
        val tvCategory = view.findViewById<TextView>(R.id.tv_detail_category)
        val tvPrice = view.findViewById<TextView>(R.id.tv_detail_price)
        val tvCycle = view.findViewById<TextView>(R.id.tv_detail_cycle)
        val tvStartDate = view.findViewById<TextView>(R.id.tv_detail_start_date)
        val tvNextBilling = view.findViewById<TextView>(R.id.tv_detail_next_billing)
        val tvCountdown = view.findViewById<TextView>(R.id.tv_detail_countdown)
        val switchReminder = view.findViewById<SwitchMaterial>(R.id.switch_detail_reminder)
        val tvNotes = view.findViewById<TextView>(R.id.tv_detail_notes)

        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        if (subscriptionId != -1) {
            viewModel.getById(subscriptionId).observe(viewLifecycleOwner) { subscription ->
                subscription?.let {
                    currentSubscription = it
                    
                    // Get brand color (same as grid and list backgrounds)
                    val brandColor = it.getBrandColor()
                    
                    // Set fragment background to the brand color
                    view.setBackgroundColor(brandColor)
                    
                    // Set avatar circle background to white and letter color to the brand color
                    tvAvatar.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
                    tvAvatar.setTextColor(brandColor)
                    
                    val initial = it.name.firstOrNull()?.uppercase() ?: "S"
                    tvAvatar.text = initial
                    
                    tvName.text = it.name
                    tvCategory.text = it.category.value
                    styleCategoryBadge(tvCategory, it.category)
                    
                    val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                    tvPrice.text = formatRupiah.format(it.price).replace("Rp", "Rp ")
                    
                    tvCycle.text = it.cycle.value
                    tvStartDate.text = it.startDate
                    tvNextBilling.text = it.nextBilling
                    
                    // Simple countdown logic (optional feature based on UI)
                    // For now just showing a placeholder or calculating days
                    tvCountdown.visibility = View.VISIBLE
                    tvCountdown.text = calculateDaysLeft(it.nextBilling)
                    
                    switchReminder.isChecked = it.reminderEnabled
                    
                    if (it.notes.isNotEmpty()) {
                        tvNotes.text = it.notes
                    } else {
                        tvNotes.text = "Tidak ada catatan"
                    }
                }
            }
        }

        btnEdit.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("subscriptionId", subscriptionId)
            }
            findNavController().navigate(R.id.addEditFragment, bundle)
        }

        btnDelete.setOnClickListener {
            currentSubscription?.let { sub ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Hapus Subscription")
                    .setMessage("Hapus ${sub.name}?")
                    .setPositiveButton("Hapus") { _, _ ->
                        AlarmUtils.cancelAlarm(requireContext(), sub.id)
                        viewModel.delete(sub)
                        Snackbar.make(requireActivity().findViewById(android.R.id.content), "Subscription ${sub.name} berhasil dihapus", Snackbar.LENGTH_LONG).show()
                        findNavController().navigateUp()
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }
        }
    }
    
    private fun calculateDaysLeft(nextBilling: String): String {
        try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val date = sdf.parse(nextBilling)
            if (date != null) {
                val today = java.util.Calendar.getInstance()
                // Reset time to start of day
                today.set(java.util.Calendar.HOUR_OF_DAY, 0)
                today.set(java.util.Calendar.MINUTE, 0)
                today.set(java.util.Calendar.SECOND, 0)
                today.set(java.util.Calendar.MILLISECOND, 0)
                
                val diff = date.time - today.timeInMillis
                val days = java.util.concurrent.TimeUnit.DAYS.convert(diff, java.util.concurrent.TimeUnit.MILLISECONDS)
                
                return if (days > 0) {
                    "$days Hari Lagi!"
                } else if (days == 0L) {
                    "Hari Ini!"
                } else {
                    "Terlewat ${-days} Hari!"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "N/A"
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
}