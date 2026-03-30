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

class TransferFragment : Fragment() {
    private var _binding: FragmentTransferBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTransferBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.transferButton.setOnClickListener { handleTransfer() }
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show() }
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.transferButton.isEnabled = loading != true
        }
    }

    private fun handleTransfer() {
        val recipientName = binding.recipientInput.text.toString().trim()
        val amountStr = binding.amountInput.text.toString().trim()
        val description = binding.descriptionInput.text.toString().trim()

        if (recipientName.isBlank()) { showError("Please enter recipient name"); return }
        if (amountStr.isBlank()) { showError("Please enter amount"); return }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) { showError("Invalid amount"); return }

        val currentBalance = viewModel.balanceResponse.value?.balance ?: 0.0
        if (amount > currentBalance) { showError("Insufficient balance"); return }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.transfer(recipientName, amount, description.ifBlank { "Transfer to $recipientName" })
                Snackbar.make(binding.root, "Transfer successful!", Snackbar.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } catch (e: Exception) {
                // error already posted to viewModel.error
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
