package com.example.smartexpensesplitter.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smartexpensesplitter.data.model.Group
import com.example.smartexpensesplitter.databinding.ItemGroupBinding

class GroupAdapter(private val onClick: (Group) -> Unit) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    private var groups = listOf<Group>()

    fun setGroups(list: List<Group>) {
        groups = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = ItemGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]
        holder.bind(group)
    }

    override fun getItemCount() = groups.size

    inner class GroupViewHolder(private val binding: ItemGroupBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(group: Group) {
            binding.tvGroupName.text = group.groupName
            binding.root.setOnClickListener { onClick(group) }
        }
    }
}

