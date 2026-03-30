package com.pcloudy.bankingg

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.pcloudy.bankingg.databinding.FragmentBillPaymentBinding
import kotlinx.coroutines.launch

class BillPaymentFragment : Fragment() {
    private var _binding: FragmentBillPaymentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBillPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBillTypeSpinner()
        setupPayBillButton()
        observeErrors()
    }

    private fun setupBillTypeSpinner() {
        val billTypes = arrayOf(
            "Select Bill Type", "Electricity Bill", "Water Bill",
            "Internet Bill", "Phone Bill", "Gas Bill", "Credit Card Bill", "Other Bill"
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, billTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.billTypeSpinner.adapter = adapter
        binding.billTypeSpinner.setSelection(0)
    }

    private fun setupPayBillButton() {
        binding.payBillButton.setOnClickListener { validateAndPay() }
    }

    private fun observeErrors() {
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show() }
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.payBillButton.isEnabled = loading != true
        }
    }

    private fun validateAndPay() {
        val selectedPos = binding.billTypeSpinner.selectedItemPosition
        val billType = binding.billTypeSpinner.selectedItem.toString()
        val amountStr = binding.billAmountInput.text.toString().trim()

        if (selectedPos == 0) { showError("Please select a bill type"); return }
        if (amountStr.isEmpty()) { showError("Please enter bill amount"); return }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) { showError("Invalid amount"); return }

        val currentBalance = viewModel.balanceResponse.value?.balance ?: 0.0
        if (amount > currentBalance) { showError("Insufficient balance"); return }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.payBill(billType, amount)
                Snackbar.make(binding.root, "Bill payment successful!", Snackbar.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } catch (e: Exception) {
                // error already posted to viewModel.error
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
