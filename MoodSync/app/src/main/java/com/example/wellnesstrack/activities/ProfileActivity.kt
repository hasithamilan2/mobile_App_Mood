package com.example.wellnesstrack.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.wellnesstrack.R
import com.example.wellnesstrack.utils.PreferenceManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    private lateinit var preferenceManager: PreferenceManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        
        // Initialize PreferenceManager
        preferenceManager = PreferenceManager(this)
        
        // Set up toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        
        // Get current user
        val currentUser = preferenceManager.getCurrentUser()
        
        // Set user information
        findViewById<TextView>(R.id.tvUsername).text = currentUser?.username ?: "User"
        findViewById<TextView>(R.id.tvEmail).text = currentUser?.email ?: "user@example.com"
        
        // Set additional information if available
        findViewById<TextView>(R.id.tvFullName).text = currentUser?.fullName ?: "Not set"
        
        // Set join date - without relying on createdAt property
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val joinDateText = if (currentUser != null) {
            // Just use current time as we don't track when users were created
            dateFormat.format(Date())
        } else {
            "Unknown"
        }
        findViewById<TextView>(R.id.tvJoinDate).text = joinDateText
        
        // Set up logout button
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            preferenceManager.logoutUser()
            
            // Navigate back to login activity
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }
    
    // Removed the updateActivityStats() method as it's no longer needed
}