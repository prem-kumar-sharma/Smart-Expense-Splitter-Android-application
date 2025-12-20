package com.example.smartexpensesplitter.ui.payment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartexpensesplitter.databinding.ActivityBalanceBinding

class BalanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBalanceBinding
    private val viewModel: BalanceViewModel by viewModels()
    private lateinit var adapter: BalanceAdapter
    private var groupId: String? = null
    
    private var pendingSettlement: Settlement? = null

    // Payment Result Launcher
    private val paymentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // Regardless of result code, ask for manual confirmation
        showConfirmationDialog()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBalanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        groupId = intent.getStringExtra("GROUP_ID")
        if (groupId == null) finish()
        
        adapter = BalanceAdapter(viewModel.getCurrentUserId()) { settlement, receiver ->
             pendingSettlement = settlement
             initiateUpiPayment(settlement.amount, receiver.upiId, receiver.name)
        }
        
        binding.rvBalances.layoutManager = LinearLayoutManager(this)
        binding.rvBalances.adapter = adapter

        groupId?.let { viewModel.loadBalances(it) }

        viewModel.settlements.observe(this) { list ->
            if (list.isEmpty()) {
                binding.tvNoDebts.visibility = View.VISIBLE
            } else {
                binding.tvNoDebts.visibility = View.GONE
            }
            val userMap = viewModel.users.value ?: emptyMap()
            adapter.setData(list, userMap)
        }
        
        viewModel.users.observe(this) { map ->
             val list = viewModel.settlements.value ?: emptyList()
             adapter.setData(list, map)
        }
    }
    
    private fun initiateUpiPayment(amount: Double, upiId: String, name: String) {
        if (upiId.isEmpty()) {
            Toast.makeText(this, "Receiver has no UPI ID linked.", Toast.LENGTH_SHORT).show()
            showConfirmationDialog() 
            return
        }
        
        val uri = Uri.parse("upi://pay").buildUpon()
            .appendQueryParameter("pa", upiId)
            .appendQueryParameter("pn", name)
            .appendQueryParameter("am", String.format("%.2f", amount))
            .appendQueryParameter("cu", "INR")
            .build()

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = uri
        
        val chooser = Intent.createChooser(intent, "Pay with")
        try {
            paymentLauncher.launch(chooser)
        } catch (e: Exception) {
            Toast.makeText(this, "No UPI app found", Toast.LENGTH_SHORT).show()
            showConfirmationDialog()
        }
    }
    
    private fun showConfirmationDialog() {
        val settlement = pendingSettlement ?: return
        
        AlertDialog.Builder(this)
            .setTitle("Confirm Payment")
            .setMessage("Did you successfully pay ₹${settlement.amount}?")
            .setPositiveButton("Yes") { _, _ ->
                groupId?.let { gid ->
                    viewModel.recordPayment(gid, settlement.payerId, settlement.receiverId, settlement.amount)
                    Toast.makeText(this, "Payment Recorded", Toast.LENGTH_SHORT).show()
                }
                pendingSettlement = null
            }
            .setNegativeButton("No") { _, _ ->
                pendingSettlement = null
            }
            .show()
    }
}

