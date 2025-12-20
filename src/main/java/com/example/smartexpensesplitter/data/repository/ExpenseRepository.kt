package com.example.smartexpensesplitter.data.repository

import com.example.smartexpensesplitter.data.model.Expense
import com.example.smartexpensesplitter.data.model.Group
import com.example.smartexpensesplitter.data.model.Payment
import com.example.smartexpensesplitter.data.model.User
import com.example.smartexpensesplitter.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ExpenseRepository {

    private val db = FirebaseFirestore.getInstance()

    // --- Groups ---

    suspend fun createGroup(groupName: String, createdBy: String, members: List<String>): Resource<String> {
        return try {
            val groupId = UUID.randomUUID().toString()
            val allMembers = members.toMutableList()
            if (!allMembers.contains(createdBy)) {
                allMembers.add(createdBy)
            }
            val group = Group(groupId, groupName, allMembers, createdBy)
            db.collection("groups").document(groupId).set(group).await()
            Resource.Success(groupId)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create group", e)
        }
    }

    suspend fun getUserGroups(userId: String): Resource<List<Group>> {
        return try {
            val snapshot = db.collection("groups")
                .whereArrayContains("members", userId)
                .get().await()
            val groups = snapshot.toObjects(Group::class.java)
            Resource.Success(groups)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch groups", e)
        }
    }

    suspend fun getGroupDetails(groupId: String): Resource<Group> {
        return try {
            val doc = db.collection("groups").document(groupId).get().await()
            val group = doc.toObject(Group::class.java)
            if (group != null) Resource.Success(group) else Resource.Error("Group not found")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error fetching group", e)
        }
    }
    
    suspend fun getUsersByIds(userIds: List<String>): Resource<List<User>> {
         return try {
             val users = mutableListOf<User>()
             // Note: In production, use FieldPath.documentId() in [...] query if list is small, 
             // but loop is safer for academic simple logic without index issues.
             for (uid in userIds) {
                 val doc = db.collection("users").document(uid).get().await()
                 doc.toObject(User::class.java)?.let { users.add(it) }
             }
             Resource.Success(users)
         } catch (e: Exception) {
             Resource.Error("Failed to fetch members", e)
         }
    }
    
    suspend fun getAllUsers(): Resource<List<User>> {
         return try {
             val snapshot = db.collection("users").get().await()
             Resource.Success(snapshot.toObjects(User::class.java))
         } catch (e: Exception) {
             Resource.Error("Failed to fetch users", e)
         }
    }

    // --- Expenses ---

    suspend fun addExpense(expense: Expense): Resource<Boolean> {
        return try {
            db.collection("expenses").document(expense.expenseId).set(expense).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add expense", e)
        }
    }

    suspend fun getGroupExpenses(groupId: String): Resource<List<Expense>> {
        return try {
            val snapshot = db.collection("expenses")
                .whereEqualTo("groupId", groupId)
                .get().await() 
            val expenses = snapshot.toObjects(Expense::class.java)
            // Sorting locally to avoid composite index requirement for now
            Resource.Success(expenses.sortedByDescending { it.timestamp })
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch expenses", e)
        }
    }

    // --- Payments ---
    
    suspend fun addPayment(payment: Payment): Resource<Boolean> {
        return try {
             db.collection("payments").document(payment.paymentId).set(payment).await()
             Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to record payment", e)
        }
    }
    
    suspend fun getGroupPayments(groupId: String): Resource<List<Payment>> {
        return try {
            val snapshot = db.collection("payments")
                .whereEqualTo("groupId", groupId)
                .get().await()
            val payments = snapshot.toObjects(Payment::class.java)
             Resource.Success(payments.sortedByDescending { it.timestamp })
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch payments", e)
        }
    }
}

