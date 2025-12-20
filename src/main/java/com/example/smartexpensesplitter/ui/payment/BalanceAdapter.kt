package com.example.smartexpensesplitter.ui.payment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smartexpensesplitter.data.model.User
import com.example.smartexpensesplitter.databinding.ItemBalanceBinding

class BalanceAdapter(
    private val currentUserId: String?,
    private val onPayClick: (Settlement, User) -> Unit
) : RecyclerView.Adapter<BalanceAdapter.BalanceViewHolder>() {

    private var settlements = listOf<Settlement>()
    private var users = mapOf<String, User>()

    fun setData(list: List<Settlement>, userMap: Map<String, User>) {
        settlements = list
        users = userMap
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BalanceViewHolder {
        val binding = ItemBalanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BalanceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BalanceViewHolder, position: Int) {
        val settlement = settlements[position]
        holder.bind(settlement)
    }

    override fun getItemCount() = settlements.size

    inner class BalanceViewHolder(private val binding: ItemBalanceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(settlement: Settlement) {
            val payerName = users[settlement.payerId]?.name ?: "Unknown"
            val receiverName = users[settlement.receiverId]?.name ?: "Unknown"
            val amount = String.format("%.2f", settlement.amount)

            if (settlement.payerId == currentUserId) {
                binding.tvText.text = "You owe $receiverName ₹$amount"
                binding.btnPay.visibility = View.VISIBLE
                binding.btnPay.setOnClickListener {
                    users[settlement.receiverId]?.let { receiver ->
                         onPayClick(settlement, receiver)
                    }
                }
            } else if (settlement.receiverId == currentUserId) {
                binding.tvText.text = "$payerName owes you ₹$amount"
                binding.btnPay.visibility = View.GONE
            } else {
                binding.tvText.text = "$payerName owes $receiverName ₹$amount"
                binding.btnPay.visibility = View.GONE
            }
        }
    }
}

