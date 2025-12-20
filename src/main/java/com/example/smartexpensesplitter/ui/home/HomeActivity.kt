package com.example.smartexpensesplitter.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartexpensesplitter.databinding.ActivityHomeBinding
import com.example.smartexpensesplitter.ui.auth.LoginActivity
import com.example.smartexpensesplitter.ui.group.CreateGroupActivity
import com.example.smartexpensesplitter.ui.group.GroupDetailsActivity
import com.example.smartexpensesplitter.utils.Resource

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: GroupAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = GroupAdapter { group ->
            val intent = Intent(this, GroupDetailsActivity::class.java)
            intent.putExtra("GROUP_ID", group.groupId)
            startActivity(intent)
        }

        binding.rvGroups.layoutManager = LinearLayoutManager(this)
        binding.rvGroups.adapter = adapter

        binding.fabCreateGroup.setOnClickListener {
            startActivity(Intent(this, CreateGroupActivity::class.java))
        }
        
        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        viewModel.groups.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    adapter.setGroups(resource.data)
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    // Show error
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadGroups()
    }
}

