package com.example.smartexpensesplitter.data.model

data class Expense(
    val expenseId: String = "",
    val groupId: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val paidBy: String = "", // User ID who paid
    val splitAmong: List<String> = emptyList(), // List of User IDs involved
    val timestamp: Long = 0
)

