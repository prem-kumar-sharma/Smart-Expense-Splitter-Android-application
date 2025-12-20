package com.example.smartexpensesplitter.ui.group

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartexpensesplitter.databinding.ActivityCreateGroupBinding
import com.example.smartexpensesplitter.utils.Resource

class CreateGroupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateGroupBinding
    private val viewModel: GroupViewModel by viewModels()
    private lateinit var adapter: UserSelectionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = UserSelectionAdapter()
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = adapter

        viewModel.loadUsers()

        binding.btnCreate.setOnClickListener {
            val name = binding.etGroupName.text.toString().trim()
            val selectedIds = adapter.selectedUserIds.toList()
            if (name.isNotEmpty() && selectedIds.isNotEmpty()) {
                viewModel.createGroup(name, selectedIds)
            } else {
                Toast.makeText(this, "Please enter name and select at least one member", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.users.observe(this) { resource ->
            if (resource is Resource.Success) {
                adapter.setUsers(resource.data)
            }
        }

        viewModel.createGroupStatus.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnCreate.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Group Created!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnCreate.isEnabled = true
                    Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

