package com.example.wellnesstrack.models

data class MoodEntry(
    val id: Long,
    val emoji: String,
    val mood: String,
    val date: String,
    val time: String,
    val note: String,
    val colorResId: Int
)