package com.example.smartexpensesplitter.ui.expense

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartexpensesplitter.data.model.Expense
import com.example.smartexpensesplitter.data.model.User
import com.example.smartexpensesplitter.data.repository.AuthRepository
import com.example.smartexpensesplitter.data.repository.ExpenseRepository
import com.example.smartexpensesplitter.utils.Resource
import kotlinx.coroutines.launch
import java.util.UUID

class ExpenseViewModel : ViewModel() {
    private val repository = ExpenseRepository()
    private val authRepository = AuthRepository()

    private val _expenses = MutableLiveData<Resource<List<Expense>>>()
    val expenses: LiveData<Resource<List<Expense>>> = _expenses
    
    private val _groupMembers = MutableLiveData<Resource<List<User>>>()
    val groupMembers: LiveData<Resource<List<User>>> = _groupMembers

    private val _addExpenseStatus = MutableLiveData<Resource<Boolean>>()
    val addExpenseStatus: LiveData<Resource<Boolean>> = _addExpenseStatus

    fun loadGroupData(groupId: String) {
        viewModelScope.launch {
            val groupRes = repository.getGroupDetails(groupId)
            if (groupRes is Resource.Success) {
                _groupMembers.value = repository.getUsersByIds(groupRes.data.members)
            }
        }
        loadExpenses(groupId)
    }

    fun loadExpenses(groupId: String) {
        _expenses.value = Resource.Loading
        viewModelScope.launch {
            _expenses.value = repository.getGroupExpenses(groupId)
        }
    }

    fun addExpense(groupId: String, description: String, amount: Double, paidBy: String, splitAmong: List<String>) {
        _addExpenseStatus.value = Resource.Loading
        val expense = Expense(
            expenseId = UUID.randomUUID().toString(),
            groupId = groupId,
            description = description,
            amount = amount,
            paidBy = paidBy,
            splitAmong = splitAmong,
            timestamp = System.currentTimeMillis()
        )
        viewModelScope.launch {
            _addExpenseStatus.value = repository.addExpense(expense)
        }
    }
    
    fun getCurrentUserId() = authRepository.getCurrentUserUid()
}

