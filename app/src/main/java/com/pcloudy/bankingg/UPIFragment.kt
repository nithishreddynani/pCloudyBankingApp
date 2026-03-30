package com.pcloudy.bankingg

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.pcloudy.bankingg.databinding.FragmentUpiBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class UPIFragment : Fragment() {
    private var _binding: FragmentUpiBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        binding.apply {
            buttonPay.setOnClickListener {
                handleUPIPayment()
            }
        }
    }

    private fun handleUPIPayment() {
        val upiId = binding.upiIdInput.text.toString().trim()
        val amount = binding.amountInput.text.toString().trim()
        val note = binding.noteInput.text.toString().trim()

        if (validateUPIPayment(upiId, amount)) {
            lifecycleScope.launch {
                try {
                    val amountValue = amount.toDouble()
                    viewModel.submitUpiPayment(upiId, amountValue)

                    // Navigate back on success
                    findNavController().navigateUp()
                } catch (e: Exception) {
                    viewModel.addFailedTransaction(upiId, amount.toDouble(), e.message ?: "Payment failed")
                    Snackbar.make(binding.root, "Payment failed: ${e.message}", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun validateUPIPayment(upiId: String, amount: String): Boolean {
        if (upiId.isBlank()) {
            showError("Please enter UPI ID")
            return false
        }

        if (!upiId.contains("@")) {
            showError("Invalid UPI ID format")
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

            val currentBalance = viewModel.balanceResponse.value?.balance ?: 0.0
            if (amountValue > currentBalance) {
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
}