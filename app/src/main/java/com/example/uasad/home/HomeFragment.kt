package com.example.uasad.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uasad.R
import com.example.uasad.data.SubscriptionCycle
import com.example.uasad.data.SubscriptionViewModel
import com.example.uasad.data.SubscriptionViewModelFactory
import com.example.uasad.data.SubsTrackerDatabase
import com.example.uasad.data.SubscriptionRepository
import com.example.uasad.data.DatabaseBuilder
import com.example.uasad.databinding.FragmentHomeBinding
import java.text.NumberFormat
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SubscriptionViewModel by activityViewModels {
        SubscriptionViewModelFactory(
            SubscriptionRepository(
                DatabaseBuilder.getInstance(requireContext().applicationContext).subscriptionDao()
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView
        val adapter = UpcomingAdapter { subscription ->
            // Opsional: navigasi ke detail
        }
        binding.rvUpcoming.layoutManager = LinearLayoutManager(context)
        binding.rvUpcoming.adapter = adapter

        // Setup FAB navigasi ke Tambah
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.addEditFragment)
        }

        // Observe upcoming bills
        viewModel.upcomingSubscriptions.observe(viewLifecycleOwner) { subscriptions ->
            adapter.submitList(subscriptions)
        }

        // Observe total spending
        viewModel.allSubscriptions.observe(viewLifecycleOwner) { subscriptions ->
            binding.tvActiveSubs.text = "${subscriptions.size} langganan aktif"
            
            var total = 0.0
            subscriptions.forEach { subscription ->
                total += when (subscription.cycle) {
                    SubscriptionCycle.WEEKLY -> subscription.price * 4
                    SubscriptionCycle.MONTHLY -> subscription.price
                    SubscriptionCycle.YEARLY -> subscription.price / 12
                }
            }
            
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            val formatted = format.format(total).replace("Rp", "Rp ")
            binding.tvTotalSpending.text = formatted
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}