package com.example.wellnesstrack.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.wellnesstrack.models.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferenceManager(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences("WellnessTrackPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        const val KEY_CURRENT_USER = "current_user"
        const val KEY_USERS = "users"
        const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    // Register a new user
    fun registerUser(user: User): Boolean {
        val users = getUsers().toMutableList()
        
        // Check if user with same email already exists
        if (users.any { it.email == user.email }) {
            return false
        }
        
        users.add(user)
        saveUsers(users)
        return true
    }
    
    // Login user
    fun loginUser(email: String, password: String): User? {
        val users = getUsers()
        val user = users.find { it.email == email && it.password == password }
        
        if (user != null) {
            // Save logged in user
            saveCurrentUser(user)
            setLoggedIn(true)
        }
        
        return user
    }
    
    // Logout user
    fun logoutUser() {
        preferences.edit().apply {
            remove(KEY_CURRENT_USER)
            putBoolean(KEY_IS_LOGGED_IN, false)
        }.apply()
    }
    
    // Check if user is logged in
    fun isLoggedIn(): Boolean {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    // Get current logged in user
    fun getCurrentUser(): User? {
        val userJson = preferences.getString(KEY_CURRENT_USER, null) ?: return null
        return gson.fromJson(userJson, User::class.java)
    }
    
    // Save current user
    private fun saveCurrentUser(user: User) {
        val userJson = gson.toJson(user)
        preferences.edit().putString(KEY_CURRENT_USER, userJson).apply()
    }
    
    // Get all users
    fun getUsers(): List<User> {
        val usersJson = preferences.getString(KEY_USERS, null) ?: return emptyList()
        val type = object : TypeToken<List<User>>() {}.type
        return gson.fromJson(usersJson, type)
    }
    
    // Save users list
    private fun saveUsers(users: List<User>) {
        val usersJson = gson.toJson(users)
        preferences.edit().putString(KEY_USERS, usersJson).apply()
    }
    
    // Set logged in status
    private fun setLoggedIn(isLoggedIn: Boolean) {
        preferences.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }
    
    // Update user profile
    fun updateUserProfile(user: User): Boolean {
        val users = getUsers().toMutableList()
        val index = users.indexOfFirst { it.email == user.email }
        
        if (index != -1) {
            users[index] = user
            saveUsers(users)
            
            // If this is the current user, update current user as well
            val currentUser = getCurrentUser()
            if (currentUser != null && currentUser.email == user.email) {
                saveCurrentUser(user)
            }
            
            return true
        }
        
        return false
    }
}