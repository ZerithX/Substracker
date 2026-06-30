package com.example.uasad

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.uasad.databinding.ActivityMainBinding
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.lifecycle.ViewModelProvider
import com.example.uasad.data.DatabaseBuilder
import com.example.uasad.data.SubscriptionRepository
import com.example.uasad.data.SubscriptionViewModel
import com.example.uasad.data.SubscriptionViewModelFactory

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: SubscriptionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle splash screen before setContentView
        installSplashScreen()
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // mencari wadah layout untuk fragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        // Setup ViewModel
        val database = DatabaseBuilder.getInstance(applicationContext)
        val repository = SubscriptionRepository(database.subscriptionDao())
        val factory = SubscriptionViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(SubscriptionViewModel::class.java)

        // logika muncul fab n bottom nav bar
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment, R.id.listFragment, R.id.settingsFragment -> {
                    binding.bottomNavigation.visibility = View.VISIBLE

                    if (destination.id != R.id.settingsFragment) {
                        binding.fabAdd?.show()
                    } else {
                        binding.fabAdd?.hide()
                    }
                } else -> { // addedit fragment
                    binding.bottomNavigation.visibility = View.GONE
                    binding.fabAdd.hide()
                }
            }
        }

        // logika navigasi fab
        binding.fabAdd.setOnClickListener {
            navController.navigate(R.id.addEditFragment)
        }
        
        // Handle Notification Intent
        val openDetailId = intent.getIntExtra("OPEN_DETAIL", -1)
        if (openDetailId != -1) {
            val bundle = Bundle().apply {
                putInt("subscriptionId", openDetailId)
            }
            navController.navigate(R.id.detailFragment, bundle)
        }
        
        // Request POST_NOTIFICATIONS runtime permission for Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkAndResetPassedSubscriptions(applicationContext)
    }

    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        val openDetailId = intent?.getIntExtra("OPEN_DETAIL", -1) ?: -1
        if (openDetailId != -1) {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
            val bundle = Bundle().apply {
                putInt("subscriptionId", openDetailId)
            }
            navController.navigate(R.id.detailFragment, bundle)
        }
    }
}