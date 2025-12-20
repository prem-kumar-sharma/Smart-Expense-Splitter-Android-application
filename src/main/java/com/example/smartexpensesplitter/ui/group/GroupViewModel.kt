package com.example.smartexpensesplitter.ui.group

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartexpensesplitter.data.model.Group
import com.example.smartexpensesplitter.data.model.User
import com.example.smartexpensesplitter.data.repository.AuthRepository
import com.example.smartexpensesplitter.data.repository.ExpenseRepository
import com.example.smartexpensesplitter.utils.Resource
import kotlinx.coroutines.launch

class GroupViewModel : ViewModel() {
    private val expenseRepository = ExpenseRepository()
    private val authRepository = AuthRepository()

    private val _users = MutableLiveData<Resource<List<User>>>()
    val users: LiveData<Resource<List<User>>> = _users

    private val _createGroupStatus = MutableLiveData<Resource<String>>()
    val createGroupStatus: LiveData<Resource<String>> = _createGroupStatus

    fun loadUsers() {
        _users.value = Resource.Loading
        viewModelScope.launch {
            _users.value = expenseRepository.getAllUsers()
        }
    }

    fun createGroup(name: String, selectedUserIds: List<String>) {
        val currentUser = authRepository.getCurrentUserUid() ?: return
        _createGroupStatus.value = Resource.Loading
        viewModelScope.launch {
            _createGroupStatus.value = expenseRepository.createGroup(name, currentUser, selectedUserIds)
        }
    }
}

