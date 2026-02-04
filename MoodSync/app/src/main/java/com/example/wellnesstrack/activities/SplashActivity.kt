package com.example.wellnesstrack.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.wellnesstrack.R

class SplashActivity : AppCompatActivity() {
    
    private val SPLASH_DISPLAY_LENGTH = 3000 // 3 seconds
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        // Find views
        val logoImageView = findViewById<ImageView>(R.id.ivLogo)
        val appNameTextView = findViewById<TextView>(R.id.tvAppName)
        
        // Create fade-in animation
        val fadeIn = AlphaAnimation(0.0f, 1.0f)
        fadeIn.duration = 1500
        
        // Apply animations
        logoImageView.startAnimation(fadeIn)
        
        // Delay the text appearance
        Handler(Looper.getMainLooper()).postDelayed({
            appNameTextView.startAnimation(fadeIn)
        }, 1000)
        
        // Navigate to main activity after delay
        Handler(Looper.getMainLooper()).postDelayed({
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
            finish()
        }, SPLASH_DISPLAY_LENGTH.toLong())
    }
}