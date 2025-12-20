package com.example.smartexpensesplitter.ui.group

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartexpensesplitter.databinding.ActivityGroupDetailsBinding
import com.example.smartexpensesplitter.ui.expense.AddExpenseActivity
import com.example.smartexpensesplitter.ui.expense.ExpenseAdapter
import com.example.smartexpensesplitter.ui.expense.ExpenseViewModel
import com.example.smartexpensesplitter.ui.payment.BalanceActivity
import com.example.smartexpensesplitter.utils.Resource

class GroupDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGroupDetailsBinding
    private val viewModel: ExpenseViewModel by viewModels()
    private lateinit var adapter: ExpenseAdapter
    private var groupId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        groupId = intent.getStringExtra("GROUP_ID")
        if (groupId == null) {
            finish()
            return
        }

        adapter = ExpenseAdapter()
        binding.rvExpenses.layoutManager = LinearLayoutManager(this)
        binding.rvExpenses.adapter = adapter

        binding.fabAddExpense.setOnClickListener {
            val intent = Intent(this, AddExpenseActivity::class.java)
            intent.putExtra("GROUP_ID", groupId)
            startActivity(intent)
        }
        
        binding.btnBalances.setOnClickListener {
             val intent = Intent(this, BalanceActivity::class.java)
             intent.putExtra("GROUP_ID", groupId)
             startActivity(intent)
        }

        viewModel.groupMembers.observe(this) { resource ->
            if (resource is Resource.Success) {
                val map = resource.data.associate { it.userId to it.name }
                adapter.setUserMap(map)
            }
        }

        viewModel.expenses.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {}
                is Resource.Success -> {
                    if (resource.data.isEmpty()) {
                        binding.tvNoExpenses.visibility = View.VISIBLE
                    } else {
                        binding.tvNoExpenses.visibility = View.GONE
                        adapter.setExpenses(resource.data)
                    }
                }
                is Resource.Error -> {}
            }
        }
    }

    override fun onResume() {
        super.onResume()
        groupId?.let { viewModel.loadGroupData(it) }
    }
}

