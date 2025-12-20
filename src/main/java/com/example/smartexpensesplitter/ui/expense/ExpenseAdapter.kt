package com.example.smartexpensesplitter.ui.expense

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smartexpensesplitter.data.model.Expense
import com.example.smartexpensesplitter.databinding.ItemExpenseBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpenseAdapter : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    private var expenses = listOf<Expense>()
    private var userIdMap = mapOf<String, String>() // ID -> Name

    fun setExpenses(list: List<Expense>) {
        expenses = list
        notifyDataSetChanged()
    }
    
    fun setUserMap(map: Map<String, String>) {
        userIdMap = map
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.bind(expense)
    }

    override fun getItemCount() = expenses.size

    inner class ExpenseViewHolder(private val binding: ItemExpenseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(expense: Expense) {
            binding.tvDescription.text = expense.description
            val payerName = userIdMap[expense.paidBy] ?: "Unknown"
            binding.tvPaidBy.text = "Paid by $payerName on ${SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(expense.timestamp))}"
            binding.tvAmount.text = "₹${expense.amount}"
        }
    }
}

