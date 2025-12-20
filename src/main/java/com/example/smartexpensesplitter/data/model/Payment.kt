package com.example.smartexpensesplitter.data.model

data class Payment(
    val paymentId: String = "",
    val groupId: String = "", // Added groupId
    val payerId: String = "",
    val receiverId: String = "",
    val amount: Double = 0.0,
    val status: String = "PENDING", // PENDING, COMPLETED
    val timestamp: Long = 0
)
