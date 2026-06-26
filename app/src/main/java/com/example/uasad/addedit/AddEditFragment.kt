package com.example.uasad.addedit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.uasad.R
import com.example.uasad.data.DatabaseBuilder
import com.example.uasad.data.Subscription
import com.example.uasad.data.SubscriptionCategory
import com.example.uasad.data.SubscriptionCycle
import com.example.uasad.data.SubscriptionRepository
import com.example.uasad.data.SubscriptionViewModel
import com.example.uasad.data.SubscriptionViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.widget.TextView
import com.example.uasad.utils.AlarmUtils
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar

class AddEditFragment : Fragment() {

    private lateinit var viewModel: SubscriptionViewModel
    private var subscriptionId: Int = -1
    private var isEditMode: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inisialisasi ViewModel
        val database = DatabaseBuilder.getInstance(requireContext())
        val repository = SubscriptionRepository(database.subscriptionDao())
        val factory = SubscriptionViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(SubscriptionViewModel::class.java)

        // Bind Views
        val btnBack = view.findViewById<ImageButton>(R.id.btn_back)
        val btnSave = view.findViewById<MaterialButton>(R.id.btn_save)
        
        val etName = view.findViewById<TextInputEditText>(R.id.et_name)
        val etPrice = view.findViewById<TextInputEditText>(R.id.et_price)
        val etNote = view.findViewById<TextInputEditText>(R.id.et_note)
        val switchReminder = view.findViewById<SwitchMaterial>(R.id.switch_reminder)
        
        val spinnerCycle = view.findViewById<AutoCompleteTextView>(R.id.spinner_cycle)
        val spinnerCategory = view.findViewById<AutoCompleteTextView>(R.id.spinner_category)
        
        val etDate = view.findViewById<TextInputEditText>(R.id.et_date)

        arguments?.let {
            subscriptionId = it.getInt("subscriptionId", -1)
            isEditMode = subscriptionId != -1
        }

        // 2. Setup Spinner Data untuk Siklus Billing
        val cycles = SubscriptionCycle.values().map { it.value }
        val cycleAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, cycles)
        spinnerCycle.setAdapter(cycleAdapter)

        // 3. Setup Spinner Data untuk Kategori
        val categories = SubscriptionCategory.values().map { it.value }
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        spinnerCategory.setAdapter(categoryAdapter)

        // Setup DatePicker untuk Tanggal Mulai
        etDate.setOnClickListener {
            showDatePickerDialog(etDate)
        }

        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        val tilName = view.findViewById<TextInputLayout>(R.id.til_name)
        val tilPrice = view.findViewById<TextInputLayout>(R.id.til_price)
        val tilDate = view.findViewById<TextInputLayout>(R.id.til_date)
        val tvTitle = view.findViewById<TextView>(R.id.tv_title)

        if (isEditMode) {
            tvTitle.text = "Edit Subscription"
            btnSave.text = "Update"
            viewModel.getById(subscriptionId).observe(viewLifecycleOwner) { subscription ->
                subscription?.let {
                    etName.setText(it.name)
                    etPrice.setText(if (it.price % 1.0 == 0.0) it.price.toInt().toString() else it.price.toString())
                    etNote.setText(it.notes)
                    switchReminder.isChecked = it.reminderEnabled
                    spinnerCycle.setText(it.cycle.value, false)
                    spinnerCategory.setText(it.category.value, false)
                    etDate.setText(it.startDate)
                }
            }
        }

        btnSave.setOnClickListener {
            // Ambil input dari user
            val name = etName.text.toString().trim()
            val priceStr = etPrice.text.toString().trim()
            val note = etNote.text.toString().trim()
            val reminder = switchReminder.isChecked
            
            val selectedCycleStr = spinnerCycle.text.toString()
            val selectedCategoryStr = spinnerCategory.text.toString()
            val startDate = etDate.text.toString().trim()

            var isValid = true

            // Validasi Nama
            if (name.isEmpty()) {
                tilName.error = "Nama tidak boleh kosong"
                isValid = false
            } else if (name.length > 100) {
                tilName.error = "Nama maksimal 100 karakter"
                isValid = false
            } else {
                tilName.error = null
            }

            // Validasi Harga
            val price = priceStr.toDoubleOrNull()
            if (price == null || price <= 0) {
                tilPrice.error = "Harga harus angka positif > 0"
                isValid = false
            } else {
                tilPrice.error = null
            }

            // Validasi Tanggal
            if (startDate.isEmpty() || startDate == "--") {
                tilDate.error = "Tanggal mulai tidak boleh kosong"
                isValid = false
            } else {
                tilDate.error = null
            }

            if (!isValid) {
                return@setOnClickListener
            }

            // Map string yang dipilih kembali ke Enum
            val selectedCycle = SubscriptionCycle.values().firstOrNull { it.value == selectedCycleStr } ?: SubscriptionCycle.MONTHLY
            val selectedCategory = SubscriptionCategory.values().firstOrNull { it.value == selectedCategoryStr } ?: SubscriptionCategory.OTHER

            // Kalkulasi next_billing
            var nextBilling = ""
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val date = java.time.LocalDate.parse(startDate, formatter)
                    val nextDate = when (selectedCycle) {
                        SubscriptionCycle.WEEKLY -> date.plusWeeks(1)
                        SubscriptionCycle.MONTHLY -> date.plusMonths(1)
                        SubscriptionCycle.YEARLY -> date.plusYears(1)
                    }
                    nextBilling = nextDate.format(formatter)
                } else {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    val date = sdf.parse(startDate)
                    if (date != null) {
                        val calendar = java.util.Calendar.getInstance()
                        calendar.time = date
                        when (selectedCycle) {
                            SubscriptionCycle.WEEKLY -> calendar.add(java.util.Calendar.WEEK_OF_YEAR, 1)
                            SubscriptionCycle.MONTHLY -> calendar.add(java.util.Calendar.MONTH, 1)
                            SubscriptionCycle.YEARLY -> calendar.add(java.util.Calendar.YEAR, 1)
                        }
                        nextBilling = sdf.format(calendar.time)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 4. Simpan ke Database melalui ViewModel
            if (isEditMode) {
                val updatedSubscription = Subscription(
                    id = subscriptionId,
                    name = name,
                    price = price!!,
                    category = selectedCategory,
                    cycle = selectedCycle,
                    startDate = startDate,
                    nextBilling = nextBilling,
                    reminderEnabled = reminder,
                    notes = note
                )
                viewModel.update(updatedSubscription)
                Toast.makeText(requireContext(), "Langganan berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                
                if (reminder) {
                    try {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val date = sdf.parse(nextBilling)
                        if (date != null) {
                            val calendar = Calendar.getInstance()
                            calendar.time = date
                            // Set alarm at 8:00 AM on the billing day, or maybe exactly the date
                            calendar.set(Calendar.HOUR_OF_DAY, 8)
                            calendar.set(Calendar.MINUTE, 0)
                            calendar.set(Calendar.SECOND, 0)
                            AlarmUtils.setAlarm(requireContext(), subscriptionId, calendar.timeInMillis)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    AlarmUtils.cancelAlarm(requireContext(), subscriptionId)
                }
                
            } else {
                val newSubscription = Subscription(
                    name = name,
                    price = price!!,
                    category = selectedCategory,
                    cycle = selectedCycle,
                    startDate = startDate,
                    nextBilling = nextBilling,
                    reminderEnabled = reminder,
                    notes = note
                )
                viewModel.insert(newSubscription)
                Toast.makeText(requireContext(), "Langganan berhasil disimpan!", Toast.LENGTH_SHORT).show()
            }
            
            findNavController().navigateUp()
        }
    }

    private fun showDatePickerDialog(etDate: TextInputEditText) {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH)
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

        val datePickerDialog = android.app.DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Format: YYYY-MM-DD
                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                etDate.setText(formattedDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }
}