package com.example.uasad.list

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uasad.R
import com.example.uasad.data.DatabaseBuilder
import com.example.uasad.data.Subscription
import com.example.uasad.data.SubscriptionCategory
import com.example.uasad.data.SubscriptionRepository
import com.example.uasad.data.SubscriptionViewModel
import com.example.uasad.data.SubscriptionViewModelFactory
import com.example.uasad.databinding.FragmentListBinding

class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SubscriptionViewModel
    private lateinit var adapter: SubscriptionAdapter

    private var isGridView = false

    companion object {
        private const val PREFS_NAME = "list_prefs"
        private const val KEY_IS_GRID_VIEW = "is_grid_view"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inisialisasi ViewModel
        val database = DatabaseBuilder.getInstance(requireContext())
        val repository = SubscriptionRepository(database.subscriptionDao())
        val factory = SubscriptionViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(SubscriptionViewModel::class.java)

        // 2. Setup RecyclerView & Adapter
        adapter = SubscriptionAdapter()
        binding.rvSubscriptions.adapter = adapter

        // Baca preferensi layout (Grid atau List) dari SharedPreferences
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        isGridView = prefs.getBoolean(KEY_IS_GRID_VIEW, false)
        updateLayoutManager()

        // 3. Setup Listener untuk Item Click
        adapter.setOnItemClickListener { subscription ->
            val bundle = Bundle().apply {
                putInt("subscriptionId", subscription.id)
            }
            findNavController().navigate(R.id.detailFragment, bundle)
        }

        // 4. Setup Toggle Layout Buttons (List & Grid)
        binding.btnLayoutList.setOnClickListener {
            if (isGridView) {
                isGridView = false
                prefs.edit().putBoolean(KEY_IS_GRID_VIEW, isGridView).apply()
                updateLayoutManager()
            }
        }
        binding.btnLayoutGrid.setOnClickListener {
            if (!isGridView) {
                isGridView = true
                prefs.edit().putBoolean(KEY_IS_GRID_VIEW, isGridView).apply()
                updateLayoutManager()
            }
        }


        // 6. Setup Category Tabs Click Listeners
        setupCategoryTabs()

        // 7. Observe Data (Mulai dengan Semua)
        observeAllSubscriptions()
    }

    private fun updateLayoutManager() {
        if (isGridView) {
            binding.rvSubscriptions.layoutManager = GridLayoutManager(requireContext(), 2)
            adapter.isGridView = true
            binding.btnLayoutList.setImageResource(R.drawable.ic_list_inactive)
            binding.btnLayoutGrid.setImageResource(R.drawable.ic_grid_active)
        } else {
            binding.rvSubscriptions.layoutManager = LinearLayoutManager(requireContext())
            adapter.isGridView = false
            binding.btnLayoutList.setImageResource(R.drawable.ic_list_active)
            binding.btnLayoutGrid.setImageResource(R.drawable.ic_grid_inactive)
        }
    }

    private fun setupCategoryTabs() {
        binding.tabAll.setOnClickListener {
            updateCategoryTabsUI(R.id.tab_all)
            observeAllSubscriptions()
        }
        binding.tabEntertainment.setOnClickListener {
            updateCategoryTabsUI(R.id.tab_entertainment)
            observeCategorySubscriptions(SubscriptionCategory.ENTERTAINMENT)
        }
        binding.tabProductivity.setOnClickListener {
            updateCategoryTabsUI(R.id.tab_productivity)
            observeCategorySubscriptions(SubscriptionCategory.PRODUCTIVITY)
        }
        binding.tabCloudStorage.setOnClickListener {
            updateCategoryTabsUI(R.id.tab_cloud_storage)
            observeCategorySubscriptions(SubscriptionCategory.CLOUDSTORAGE)
        }
        binding.tabEducation.setOnClickListener {
            updateCategoryTabsUI(R.id.tab_education)
            observeCategorySubscriptions(SubscriptionCategory.EDUCATION)
        }
        binding.tabGaming.setOnClickListener {
            updateCategoryTabsUI(R.id.tab_gaming)
            observeCategorySubscriptions(SubscriptionCategory.GAMING)
        }
        binding.tabOther.setOnClickListener {
            updateCategoryTabsUI(R.id.tab_other)
            observeCategorySubscriptions(SubscriptionCategory.OTHER)
        }
    }

    private fun updateCategoryTabsUI(selectedTabId: Int) {
        val tabs = listOf(
            binding.tabAll to R.id.tab_all,
            binding.tabEntertainment to R.id.tab_entertainment,
            binding.tabProductivity to R.id.tab_productivity,
            binding.tabCloudStorage to R.id.tab_cloud_storage,
            binding.tabEducation to R.id.tab_education,
            binding.tabGaming to R.id.tab_gaming,
            binding.tabOther to R.id.tab_other
        )

        for ((textView, id) in tabs) {
            if (id == selectedTabId) {
                textView.setBackgroundResource(R.drawable.bg_tab_selected)
                textView.setTextColor(resources.getColor(R.color.white, null))
                textView.setTypeface(null, Typeface.BOLD)
            } else {
                textView.setBackgroundResource(R.drawable.bg_tab_unselected)
                textView.setTextColor(resources.getColor(R.color.color_70_69_87, null))
                textView.setTypeface(null, Typeface.NORMAL)
            }
        }
    }

    private fun observeAllSubscriptions() {
        // Hentikan observasi kategori spesifik jika ada
        viewModel.subscriptionsByCategory.removeObservers(viewLifecycleOwner)

        viewModel.allSubscriptions.observe(viewLifecycleOwner) { list ->
            handleDataList(list)
        }
    }

    private fun observeCategorySubscriptions(category: SubscriptionCategory) {
        // Hentikan observasi semua jika ada
        viewModel.allSubscriptions.removeObservers(viewLifecycleOwner)

        viewModel.setCategory(category)
        viewModel.subscriptionsByCategory.observe(viewLifecycleOwner) { list ->
            handleDataList(list)
        }
    }

    private fun handleDataList(list: List<Subscription>?) {
        if (list.isNullOrEmpty()) {
            binding.rvSubscriptions.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
            adapter.submitList(emptyList())
        } else {
            binding.rvSubscriptions.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
            adapter.submitList(list)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}