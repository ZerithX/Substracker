package com.example.uasad.splash

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.uasad.R
import com.example.uasad.databinding.FragmentSplashBinding

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Wait for 2 seconds then navigate
        Handler(Looper.getMainLooper()).postDelayed({
            // Ensure fragment is still attached before navigating
            if (isAdded) {
                val openDetailId = requireActivity().intent.getIntExtra("OPEN_DETAIL", -1)
                
                if (openDetailId != -1) {
                    // Remove extra so it doesn't trigger again (e.g. on rotation)
                    requireActivity().intent.removeExtra("OPEN_DETAIL")
                    
                    val bundle = Bundle().apply {
                        putInt("subscriptionId", openDetailId)
                    }
                    
                    // Navigate to Home first, so it's in the backstack
                    findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
                    // Then navigate to Detail
                    findNavController().navigate(R.id.detailFragment, bundle)
                    
                } else {
                    findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
                }
            }
        }, 2000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
