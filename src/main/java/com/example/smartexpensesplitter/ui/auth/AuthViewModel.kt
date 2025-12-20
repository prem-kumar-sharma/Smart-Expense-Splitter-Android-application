package com.example.smartexpensesplitter.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartexpensesplitter.data.model.User
import com.example.smartexpensesplitter.data.repository.AuthRepository
import com.example.smartexpensesplitter.utils.Resource
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()
    
    private val _authState = MutableLiveData<Resource<User>>()
    val authState: LiveData<Resource<User>> = _authState

    fun login(email: String, pass: String) {
        _authState.value = Resource.Loading
        viewModelScope.launch {
            _authState.value = repository.login(email, pass)
        }
    }

    fun register(name: String, email: String, pass: String, upiId: String) {
        _authState.value = Resource.Loading
        viewModelScope.launch {
            _authState.value = repository.register(name, email, pass, upiId)
        }
    }
    
    fun isLoggedIn(): Boolean {
        return repository.getCurrentUserUid() != null
    }
}

