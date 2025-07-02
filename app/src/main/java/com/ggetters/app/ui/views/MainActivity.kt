package com.ggetters.app.ui.views

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ggetters.app.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.setOnItemSelectedListener { item ->
            val newFragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_profile -> ProfileFragment()
                R.id.nav_settings -> SettingsFragment()
                else -> HomeFragment()
            }
            switchFragment(newFragment)
            true
        }
        // Set default fragment
        if (savedInstanceState == null) {
            switchFragment(HomeFragment())
        }
    }

    private fun switchFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        if (currentFragment == null) {
            transaction.replace(R.id.fragmentContainer, fragment)
        } else {
            transaction.setCustomAnimations(
                android.R.anim.slide_in_left, android.R.anim.fade_out,
                android.R.anim.fade_in, android.R.anim.slide_out_right
            )
            transaction.replace(R.id.fragmentContainer, fragment)
        }
        transaction.commit()
        currentFragment = fragment
    }
} 