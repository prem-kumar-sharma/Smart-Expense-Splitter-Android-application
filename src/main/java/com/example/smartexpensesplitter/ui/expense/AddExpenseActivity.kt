package com.example.smartexpensesplitter.ui.expense

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartexpensesplitter.data.model.User
import com.example.smartexpensesplitter.databinding.ActivityAddExpenseBinding
import com.example.smartexpensesplitter.ui.group.UserSelectionAdapter
import com.example.smartexpensesplitter.utils.Resource

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddExpenseBinding
    private val viewModel: ExpenseViewModel by viewModels()
    private lateinit var adapter: UserSelectionAdapter
    private var groupId: String? = null
    private var users = listOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        groupId = intent.getStringExtra("GROUP_ID")
        if (groupId == null) finish()

        adapter = UserSelectionAdapter()
        binding.rvSplitMembers.layoutManager = LinearLayoutManager(this)
        binding.rvSplitMembers.adapter = adapter

        groupId?.let { viewModel.loadGroupData(it) }

        binding.btnAdd.setOnClickListener {
            val desc = binding.etDescription.text.toString().trim()
            val amountStr = binding.etAmount.text.toString().trim()
            val amount = amountStr.toDoubleOrNull()
            
            // Get selected payer
            val payerIndex = binding.spinnerPaidBy.selectedItemPosition
            val payerId = if (users.isNotEmpty() && payerIndex >= 0) users[payerIndex].userId else ""

            val splitAmong = adapter.selectedUserIds.toList()

            if (desc.isNotEmpty() && amount != null && amount > 0 && payerId.isNotEmpty() && splitAmong.isNotEmpty()) {
                viewModel.addExpense(groupId!!, desc, amount, payerId, splitAmong)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.groupMembers.observe(this) { resource ->
            if (resource is Resource.Success) {
                users = resource.data
                adapter.setUsers(users)
                // Select all by default
                users.forEach { adapter.selectedUserIds.add(it.userId) }
                adapter.notifyDataSetChanged()
                
                // Setup Spinner
                val names = users.map { it.name }
                val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerPaidBy.adapter = spinnerAdapter
                
                // Select current user in spinner
                val currentUid = viewModel.getCurrentUserId()
                val index = users.indexOfFirst { it.userId == currentUid }
                if (index != -1) {
                    binding.spinnerPaidBy.setSelection(index)
                }
            }
        }

        viewModel.addExpenseStatus.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Expense Added", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

