package com.example.wellnesstrack.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wellnesstrack.R
import com.example.wellnesstrack.models.User
import com.example.wellnesstrack.utils.PreferenceManager
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {

    private lateinit var preferenceManager: PreferenceManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        
        preferenceManager = PreferenceManager(this)
        
        val etFullName = findViewById<TextInputEditText>(R.id.etFullName)
        val etUsername = findViewById<TextInputEditText>(R.id.etUsername)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLoginPrompt = findViewById<TextView>(R.id.tvLoginPrompt)
        
        btnRegister.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()
            
            // Validation
            if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || 
                password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, getString(R.string.error_invalid_email), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (password != confirmPassword) {
                Toast.makeText(this, getString(R.string.error_password_match), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Create user object
            val user = User(
                email = email,
                username = username,
                password = password,
                fullName = fullName
            )
            
            // Register user
            val isRegistered = preferenceManager.registerUser(user)
            
            if (isRegistered) {
                Toast.makeText(this, getString(R.string.success_registration), Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, getString(R.string.error_user_exists), Toast.LENGTH_SHORT).show()
            }
        }
        
        tvLoginPrompt.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}