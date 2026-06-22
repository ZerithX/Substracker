package com.example.uasad

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.uasad.databinding.ActivityMainBinding
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // mencari wadah layout untuk fragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

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
    }
}