package com.example.wellnesstrack.models

import java.io.Serializable
import java.util.UUID

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var icon: String = "📝", // Using emoji as default icon
    var goal: Int = 1,
    var unit: String = "times",
    var createdAt: Long = System.currentTimeMillis()
) : Serializable