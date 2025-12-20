package com.example.smartexpensesplitter.ui.group

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smartexpensesplitter.data.model.User
import com.example.smartexpensesplitter.databinding.ItemUserSelectionBinding

class UserSelectionAdapter : RecyclerView.Adapter<UserSelectionAdapter.UserViewHolder>() {

    private var users = listOf<User>()
    val selectedUserIds = mutableSetOf<String>()

    fun setUsers(list: List<User>) {
        users = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)
    }

    override fun getItemCount() = users.size

    inner class UserViewHolder(private val binding: ItemUserSelectionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.cbUser.text = "${user.name} (${user.email})"
            // Avoid triggering listener during bind
            binding.cbUser.setOnCheckedChangeListener(null)
            binding.cbUser.isChecked = selectedUserIds.contains(user.userId)
            
            binding.cbUser.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedUserIds.add(user.userId)
                } else {
                    selectedUserIds.remove(user.userId)
                }
            }
        }
    }
}

