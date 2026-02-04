package com.example.wellnesstrack.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.wellnesstrack.R
import com.example.wellnesstrack.fragments.HabitsFragment
import com.example.wellnesstrack.fragments.HomeFragment
import com.example.wellnesstrack.fragments.HydrationFragment
import com.example.wellnesstrack.fragments.MoodFragment
import com.example.wellnesstrack.utils.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferenceManager = PreferenceManager(this)

        // Set up toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        // Set up bottom navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    loadFragment(HomeFragment())
                    supportActionBar?.title = getString(R.string.title_home)
                    true
                }
                R.id.navigation_hydration -> {
                    loadFragment(HydrationFragment())
                    supportActionBar?.title = getString(R.string.title_hydration)
                    true
                }
                R.id.navigation_habits -> {
                    loadFragment(HabitsFragment())
                    supportActionBar?.title = getString(R.string.title_habits)
                    true
                }
                R.id.navigation_mood -> {
                    loadFragment(MoodFragment())
                    supportActionBar?.title = getString(R.string.title_mood)
                    true
                }
                else -> false
            }
        }

        // Set default fragment
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.navigation_home
        }
    }

    // Implement options menu creation
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    // Handle menu item clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                // Navigate to profile activity
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_logout -> {
                // Handle logout
                preferenceManager.logoutUser()

                // Navigate to login activity
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Function to help navigate to specific tabs from other parts of the app
    fun navigateToTab(itemId: Int) {
        findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId = itemId
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}