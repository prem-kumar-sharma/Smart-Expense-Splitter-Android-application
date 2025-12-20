package com.example.smartexpensesplitter.data.model

data class Group(
    val groupId: String = "",
    val groupName: String = "",
    val members: List<String> = emptyList(), // List of User IDs
    val createdBy: String = ""
)

