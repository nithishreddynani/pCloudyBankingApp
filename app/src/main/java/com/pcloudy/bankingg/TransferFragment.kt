package com.pcloudy.bankingg

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.pcloudy.bankingg.databinding.FragmentTransferBinding
import kotlinx.coroutines.launch
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TransferFragment : Fragment() {
    private var _binding: FragmentTransferBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransferBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        binding.apply {
            transferButton.setOnClickListener {
                handleTransfer()
            }
        }
    }

    private fun handleTransfer() {
        val recipientName = binding.recipientInput.text.toString()
        val amount = binding.amountInput.text.toString()
        val description = binding.descriptionInput.text.toString()
        val isInstant = binding.instantTransfer.isChecked

        if (validateTransfer(recipientName, amount)) {
            lifecycleScope.launch {
                try {
                    val amountValue = amount.toDouble()
                    viewModel.simulateApiCall("/api/transfer") {
                        withContext(Dispatchers.Main) {  // Switch to main thread
                            viewModel.updateBalance(-amountValue)

                            // Add transaction
                            val newTransaction = Transaction(
                                id = UUID.randomUUID().toString(),
                                amount = -amountValue,
                                type = "transfer",
                                description = "Transfer to $recipientName",
                                date = System.currentTimeMillis(),
                                status = "Completed"
                            )

                            viewModel.addTransaction(newTransaction)

                            // Navigate back
                            findNavController().navigateUp()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Snackbar.make(binding.root, "Transfer failed: ${e.message}", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun validateTransfer(recipient: String, amount: String): Boolean {
        if (recipient.isBlank()) {
            showError("Please enter recipient name")
            return false
        }

        if (amount.isBlank()) {
            showError("Please enter amount")
            return false
        }

        try {
            val amountValue = amount.toDouble()
            if (amountValue <= 0) {
                showError("Amount must be greater than 0")
                return false
            }

            if (amountValue > viewModel.balance.value ?: 0.0) {
                showError("Insufficient balance")
                return false
            }
        } catch (e: NumberFormatException) {
            showError("Invalid amount")
            return false
        }

        return true
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun processTransfer() {
        val recipientName = binding.recipientInput.text.toString()
        val amountStr = binding.amountInput.text.toString()
        val amount = amountStr.toDoubleOrNull() ?: 0.0

        // Validate transfer
        if (validateTransfer(amount)) {
            // Deduct amount from balance
            viewModel.updateBalance(-amount)

            // Create transfer transaction
            val transferTransaction = Transaction(
                id = UUID.randomUUID().toString(),
                amount = -amount,
                type = "transfer",
                description = "Transfer to $recipientName",
                date = System.currentTimeMillis(),
                status = "Completed"
            )

            // Add transaction to recent transactions
            viewModel.addTransaction(transferTransaction)

            // Show success message
            Snackbar.make(
                binding.root,
                "Transfer successful",
                Snackbar.LENGTH_SHORT
            ).show()

            // Navigate back to dashboard
            findNavController().navigateUp()
        }
    }

    private fun validateTransfer(amount: Double): Boolean {
        val currentBalance = viewModel.balance.value ?: 0.0

        if (amount <= 0) {
            showError("Invalid transfer amount")
            return false
        }

        if (amount > currentBalance) {
            showError("Insufficient balance")
            return false
        }

        return true
    }

}