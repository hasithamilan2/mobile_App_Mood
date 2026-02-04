package com.example.wellnesstrack.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wellnesstrack.R
import com.example.wellnesstrack.utils.PreferenceManager
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var preferenceManager: PreferenceManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        preferenceManager = PreferenceManager(this)
        
        // Check if user is already logged in
        if (preferenceManager.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegisterPrompt = findViewById<TextView>(R.id.tvRegisterPrompt)
        
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val user = preferenceManager.loginUser(email, password)
            
            if (user != null) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, getString(R.string.error_login_failed), Toast.LENGTH_SHORT).show()
            }
        }
        
        tvRegisterPrompt.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}