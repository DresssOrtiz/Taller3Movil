package com.example.taller3movil.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val online: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val photoUrl: String = ""
)
