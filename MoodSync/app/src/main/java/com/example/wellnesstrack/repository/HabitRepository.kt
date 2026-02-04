package com.example.wellnesstrack.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.wellnesstrack.models.Habit
import com.example.wellnesstrack.models.HabitProgress
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HabitRepository(private val context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "habits_preferences", Context.MODE_PRIVATE
    )
    
    private val gson = Gson()
    
    // CRUD Operations for Habits
    
    fun getAllHabits(): List<Habit> {
        val json = sharedPreferences.getString("habits", null) ?: return emptyList()
        val type = object : TypeToken<List<Habit>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
    
    fun getHabitById(habitId: String): Habit? {
        return getAllHabits().find { it.id == habitId }
    }
    
    fun saveHabit(habit: Habit) {
        val habits = getAllHabits().toMutableList()
        val index = habits.indexOfFirst { it.id == habit.id }
        
        if (index != -1) {
            // Update existing habit
            habits[index] = habit
        } else {
            // Add new habit
            habits.add(habit)
        }
        
        saveAllHabits(habits)
        
        // Log for debugging
        android.util.Log.d("HabitRepository", "Saved habit: $habit")
        android.util.Log.d("HabitRepository", "Total habits after save: ${getAllHabits().size}")
    }
    
    fun deleteHabit(habitId: String): Boolean {
        val habits = getAllHabits().toMutableList()
        val initialSize = habits.size
        habits.removeAll { it.id == habitId }
        
        if (habits.size < initialSize) {
            saveAllHabits(habits)
            // Also delete all progress related to this habit
            deleteHabitProgress(habitId)
            return true
        }
        
        return false
    }
    
    private fun saveAllHabits(habits: List<Habit>) {
        val json = gson.toJson(habits)
        
        // Use commit() instead of apply() to ensure immediate write
        val success = sharedPreferences.edit().putString("habits", json).commit()
        
        // Log for debugging
        android.util.Log.d("HabitRepository", "Saved habits to SharedPreferences, success: $success")
        android.util.Log.d("HabitRepository", "JSON saved: $json")
    }
    
    // Habit Progress Management
    
    fun getTodayDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
    
    fun getHabitProgressForToday(habitId: String): HabitProgress {
        val allProgress = getAllHabitProgress()
        val today = getTodayDate()
        return allProgress.find { it.habitId == habitId && it.date == today }
            ?: HabitProgress(habitId, today)
    }
    
    fun getAllHabitProgress(): List<HabitProgress> {
        val json = sharedPreferences.getString("habit_progress", null) ?: return emptyList()
        val type = object : TypeToken<List<HabitProgress>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
    
    fun updateHabitProgress(progress: HabitProgress) {
        val allProgress = getAllHabitProgress().toMutableList()
        val index = allProgress.indexOfFirst { 
            it.habitId == progress.habitId && it.date == progress.date 
        }
        
        if (index != -1) {
            allProgress[index] = progress
        } else {
            allProgress.add(progress)
        }
        
        saveAllProgress(allProgress)
    }
    
    private fun deleteHabitProgress(habitId: String) {
        val allProgress = getAllHabitProgress().toMutableList()
        allProgress.removeAll { it.habitId == habitId }
        saveAllProgress(allProgress)
    }
    
    private fun saveAllProgress(progressList: List<HabitProgress>) {
        val json = gson.toJson(progressList)
        sharedPreferences.edit().putString("habit_progress", json).apply()
    }
    
    fun markHabitAsComplete(habitId: String) {
        val habit = getHabitById(habitId) ?: return
        val progress = getHabitProgressForToday(habitId)
        
        progress.progress = habit.goal
        progress.isCompleted = true
        updateHabitProgress(progress)
    }
    
    fun getTodayCompletionStats(): Pair<Int, Int> {
        val habits = getAllHabits()
        if (habits.isEmpty()) return Pair(0, 0)
        
        val today = getTodayDate()
        val allProgress = getAllHabitProgress()
        
        val completed = habits.count { habit ->
            allProgress.any { 
                it.habitId == habit.id && it.date == today && it.isCompleted 
            }
        }
        
        return Pair(completed, habits.size)
    }
}