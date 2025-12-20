package com.example.smartexpensesplitter.data.repository

import com.example.smartexpensesplitter.data.model.User
import com.example.smartexpensesplitter.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun login(email: String, pass: String): Resource<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val uid = result.user?.uid
            if (uid != null) {
                // Fetch user details from Firestore
                val doc = db.collection("users").document(uid).get().await()
                val user = doc.toObject(User::class.java)
                if (user != null) {
                    Resource.Success(user)
                } else {
                    // Fallback if firestore record missing but auth success
                    Resource.Success(User(uid, result.user?.displayName ?: "", email))
                }
            } else {
                Resource.Error("Authentication failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Login failed", e)
        }
    }

    suspend fun register(name: String, email: String, pass: String, upiId: String): Resource<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = result.user?.uid
            if (uid != null) {
                val user = User(uid, name, email, upiId)
                db.collection("users").document(uid).set(user).await()
                Resource.Success(user)
            } else {
                Resource.Error("Registration failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Registration failed", e)
        }
    }

    fun getCurrentUserUid(): String? {
        return auth.currentUser?.uid
    }

    fun logout() {
        auth.signOut()
    }
}

