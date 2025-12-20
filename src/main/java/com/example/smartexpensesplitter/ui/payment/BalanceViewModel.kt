package com.example.smartexpensesplitter.ui.payment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartexpensesplitter.data.model.User
import com.example.smartexpensesplitter.data.repository.AuthRepository
import com.example.smartexpensesplitter.data.repository.ExpenseRepository
import com.example.smartexpensesplitter.utils.Resource
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min

data class Settlement(
    val payerId: String,
    val receiverId: String,
    val amount: Double
)

class BalanceViewModel : ViewModel() {
    private val repository = ExpenseRepository()
    private val authRepository = AuthRepository()

    private val _settlements = MutableLiveData<List<Settlement>>()
    val settlements: LiveData<List<Settlement>> = _settlements
    
    private val _users = MutableLiveData<Map<String, User>>()
    val users: LiveData<Map<String, User>> = _users

    fun loadBalances(groupId: String) {
        viewModelScope.launch {
            // 1. Fetch Data
            val expensesRes = repository.getGroupExpenses(groupId)
            val paymentsRes = repository.getGroupPayments(groupId)
            val groupRes = repository.getGroupDetails(groupId)
            
            if (expensesRes is Resource.Success && paymentsRes is Resource.Success && groupRes is Resource.Success) {
                val expenses = expensesRes.data
                val payments = paymentsRes.data
                val members = groupRes.data.members
                
                // Fetch Users
                val usersRes = repository.getUsersByIds(members)
                if (usersRes is Resource.Success) {
                    _users.value = usersRes.data.associateBy { it.userId }
                }

                // 2. Calculate Balances
                val balances = mutableMapOf<String, Double>()
                members.forEach { balances[it] = 0.0 }

                // Process Expenses
                expenses.forEach { expense ->
                    val paidBy = expense.paidBy
                    val amount = expense.amount
                    val splitAmong = expense.splitAmong
                    
                    if (splitAmong.isNotEmpty()) {
                        val splitAmount = amount / splitAmong.size
                        
                        // Payer gets credit
                        balances[paidBy] = (balances[paidBy] ?: 0.0) + amount
                        
                        // Consumers get debit
                        splitAmong.forEach { userId ->
                             balances[userId] = (balances[userId] ?: 0.0) - splitAmount
                        }
                    }
                }

                // Process Payments (Settlements)
                payments.forEach { payment ->
                    val payer = payment.payerId
                    val receiver = payment.receiverId
                    val amount = payment.amount
                    
                    // Payer paid debt (Credit increase)
                    balances[payer] = (balances[payer] ?: 0.0) + amount
                    // Receiver got money (Debit increase / Credit decrease)
                    balances[receiver] = (balances[receiver] ?: 0.0) - amount
                }

                // 3. Simplify Debts
                val debtors = balances.filter { it.value < -0.01 }.mapValues { abs(it.value) }.toMutableMap()
                val creditors = balances.filter { it.value > 0.01 }.toMutableMap()
                
                val settlementList = mutableListOf<Settlement>()
                
                // Greedy matching
                while (debtors.isNotEmpty() && creditors.isNotEmpty()) {
                    val debtor = debtors.keys.first()
                    val creditor = creditors.keys.first()
                    
                    val debtAmount = debtors[debtor] ?: 0.0
                    val creditAmount = creditors[creditor] ?: 0.0
                    
                    val amount = min(debtAmount, creditAmount)
                    
                    settlementList.add(Settlement(debtor, creditor, amount))
                    
                    // Update remaining
                    if (abs(debtAmount - amount) < 0.01) debtors.remove(debtor) else debtors[debtor] = debtAmount - amount
                    if (abs(creditAmount - amount) < 0.01) creditors.remove(creditor) else creditors[creditor] = creditAmount - amount
                }
                
                _settlements.value = settlementList
            }
        }
    }
    
    fun getCurrentUserId() = authRepository.getCurrentUserUid()

    fun recordPayment(groupId: String, payerId: String, receiverId: String, amount: Double) {
         val payment = com.example.smartexpensesplitter.data.model.Payment(
             paymentId = java.util.UUID.randomUUID().toString(),
             groupId = groupId,
             payerId = payerId,
             receiverId = receiverId,
             amount = amount,
             status = "COMPLETED",
             timestamp = System.currentTimeMillis()
         )
         viewModelScope.launch {
             repository.addPayment(payment)
             loadBalances(groupId) // Refresh
         }
    }
}

