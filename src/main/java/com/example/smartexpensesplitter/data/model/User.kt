package com.example.smartexpensesplitter.data.model

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val upiId: String = "" // Added UPI ID
)
