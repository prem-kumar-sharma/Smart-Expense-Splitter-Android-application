package com.example.smartexpensesplitter.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartexpensesplitter.data.model.Group
import com.example.smartexpensesplitter.data.repository.AuthRepository
import com.example.smartexpensesplitter.data.repository.ExpenseRepository
import com.example.smartexpensesplitter.utils.Resource
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val expenseRepository = ExpenseRepository()
    private val authRepository = AuthRepository()

    private val _groups = MutableLiveData<Resource<List<Group>>>()
    val groups: LiveData<Resource<List<Group>>> = _groups

    fun loadGroups() {
        val uid = authRepository.getCurrentUserUid()
        if (uid != null) {
            _groups.value = Resource.Loading
            viewModelScope.launch {
                _groups.value = expenseRepository.getUserGroups(uid)
            }
        }
    }
    
    fun logout() {
        authRepository.logout()
    }
}

