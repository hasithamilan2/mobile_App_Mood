package com.example.wellnesstrack.models

data class User(
    val email: String,
    val username: String,
    val password: String,
    val fullName: String,
    val createdAt: Long? = null
)