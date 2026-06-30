package com.example.uasad.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.example.uasad.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val PREFS_NAME = "settings_prefs"
        const val KEY_REMINDER_VALUE = "reminder_value"

        /**
         * Membaca nilai reminder dari SharedPreferences dan mengonversinya ke integer hari.
         * Contoh: "H-3" → 3, "H-7" → 7. Default ke 1 jika tidak ditemukan.
         */
        fun getReminderDaysBefore(context: Context): Int {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val raw = prefs.getString(KEY_REMINDER_VALUE, "H-1") ?: "H-1"
            return raw.removePrefix("H-").toIntOrNull() ?: 1
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupReminder()
        setupAboutSection()
    }

    /**
     * Setup dropdown pengingat pembayaran menggunakan PopupMenu.
     * Pilihan disimpan di SharedPreferences.
     */
    private fun setupReminder() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val selectedReminder = prefs.getString(KEY_REMINDER_VALUE, "H-1") ?: "H-1"

        binding.tvDropdownValue.text = selectedReminder

        binding.dropdownReminderPill.setOnClickListener { view ->
            val popup = PopupMenu(requireContext(), view)
            popup.menu.add("H-1 (1 hari sebelum)")
            popup.menu.add("H-2 (2 hari sebelum)")
            popup.menu.add("H-3 (3 hari sebelum)")
            popup.menu.add("H-7 (7 hari sebelum)")

            popup.setOnMenuItemClickListener { item ->
                val text = item.title.toString()
                // Ambil string pendeknya (misal "H-1")
                val shortVal = text.substringBefore(" ")
                
                prefs.edit().putString(KEY_REMINDER_VALUE, shortVal).apply()
                binding.tvDropdownValue.text = shortVal
                
                Toast.makeText(
                    requireContext(),
                    "Waktu reminder diubah ke $shortVal",
                    Toast.LENGTH_SHORT
                ).show()
                true
            }
            popup.show()
        }
    }

    /**
     * Setup section Tentang Aplikasi.
     * Mengisi versi aplikasi secara dinamis jika memungkinkan.
     */
    private fun setupAboutSection() {
        try {
            val packageInfo = requireContext().packageManager
                .getPackageInfo(requireContext().packageName, 0)
            binding.aboutAppVersion.text = "Versi ${packageInfo.versionName}"
        } catch (_: Exception) {
            // Gunakan default "Versi 1.0" dari XML
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}