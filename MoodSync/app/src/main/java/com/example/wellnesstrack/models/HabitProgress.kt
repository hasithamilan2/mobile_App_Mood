package com.example.wellnesstrack.models

data class HabitProgress(
    val habitId: String,
    val date: String, // Format: yyyy-MM-dd
    var progress: Int = 0,
    var isCompleted: Boolean = false
)