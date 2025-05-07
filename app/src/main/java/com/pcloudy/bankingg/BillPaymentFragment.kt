package com.pcloudy.bankingg

import android.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.pcloudy.bankingg.databinding.FragmentBillPaymentBinding
import kotlinx.coroutines.launch
import java.util.UUID

//class BillPaymentFragment : Fragment() {
//    private var _binding: FragmentBillPaymentBinding? = null
//    private val binding get() = _binding!!
//
//    private val mainViewModel: MainViewModel by activityViewModels()
//    private val billViewModel: BillViewModel by viewModels()
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentBillPaymentBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        setupBillTypeDropdown()
//        setupPayBillButton()
//    }
//
//    private fun setupBillTypeDropdown() {
//        val billTypes = arrayOf(
//            "Electricity",
//            "Water",
//            "Internet",
//            "Phone",
//            "Gas",
//            "Other"
//        )
//
//        (binding.billTypeInput as? MaterialAutoCompleteTextView)?.setSimpleItems(billTypes)
//    }
//
//    private fun setupPayBillButton() {
//        binding.payBillButton.setOnClickListener {
//            validateAndProcessBillPayment()
//        }
//    }
//
//    private fun validateAndProcessBillPayment() {
//        val billType = binding.billTypeInput.text.toString().trim()
//        val billAmountStr = binding.billAmountInput.text.toString().trim()
//
//        // Validate inputs
//        if (billType.isEmpty()) {
//            binding.billTypeLayout.error = "Please select bill type"
//            return
//        }
//
//        if (billAmountStr.isEmpty()) {
//            binding.billAmountLayout.error = "Please enter bill amount"
//            return
//        }
//
//        val billAmount = billAmountStr.toDoubleOrNull()
//        if (billAmount == null || billAmount <= 0) {
//            binding.billAmountLayout.error = "Invalid amount"
//            return
//        }
//
//        // Check if sufficient balance
//        val currentBalance = mainViewModel.balance.value ?: 0.0
//        if (billAmount > currentBalance) {
//            showInsufficientBalanceError()
//            return
//        }
//
//        // Process bill payment
//        processBillPayment(billType, billAmount)
//    }
//
//    private fun processBillPayment(billType: String, amount: Double) {
//        // Deduct amount from balance
//        mainViewModel.updateBalance(-amount)
//
//        // Create bill transaction
//        val transaction = Transaction(
//            id = UUID.randomUUID().toString(),
//            amount = -amount,
//            type = "bill",
//            description = "$billType Bill Payment",
//            date = System.currentTimeMillis(),
//            status = "Completed"
//        )
//
//        // Add transaction to recent transactions
//        mainViewModel.addTransaction(transaction)
//
//        // Show success message
//        Snackbar.make(
//            binding.root,
//            "Bill payment successful",
//            Snackbar.LENGTH_SHORT
//        ).show()
//
//        // Navigate back to dashboard
//        findNavController().navigateUp()
//    }
//
//    private fun showInsufficientBalanceError() {
//        MaterialAlertDialogBuilder(requireContext())
//            .setTitle("Insufficient Balance")
//            .setMessage("You do not have enough balance to pay this bill.")
//            .setPositiveButton("OK", null)
//            .show()
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}

class BillPaymentFragment : Fragment() {
    private var _binding: FragmentBillPaymentBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Use null check and logging
        try {
            _binding = FragmentBillPaymentBinding.inflate(inflater, container, false)
            Log.d("BillPaymentFragment", "Binding created successfully")
            return binding.root
        } catch (e: Exception) {
            Log.e("BillPaymentFragment", "Error creating binding", e)
            throw e
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            setupBillTypeSpinner()
            setupPayBillButton()
            Log.d("BillPaymentFragment", "View setup completed")
        } catch (e: Exception) {
            Log.e("BillPaymentFragment", "Error setting up view", e)
            Snackbar.make(view, "Error initializing page", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun setupBillTypeSpinner() {
        val billTypes = arrayOf(
            "Select Bill Type",
            "Electricity Bill",
            "Water Bill",
            "Internet Bill",
            "Phone Bill",
            "Gas Bill",
            "Credit Card Bill",
            "Other Bill"
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            billTypes
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.billTypeSpinner.adapter = adapter
        binding.billTypeSpinner.setSelection(0)
    }

    private fun setupPayBillButton() {
        binding.payBillButton.setOnClickListener {
            validateAndProcessBillPayment()
        }
    }

    private fun validateAndProcessBillPayment() {
        val selectedBillTypePosition = binding.billTypeSpinner.selectedItemPosition
        val billType = binding.billTypeSpinner.selectedItem.toString()
        val billAmountStr = binding.billAmountInput.text.toString().trim()

        // Validate bill type
        if (selectedBillTypePosition == 0) {
            showError("Please select a bill type")
            return
        }

        // Validate amount
        if (billAmountStr.isEmpty()) {
            showError("Please enter bill amount")
            return
        }

        val billAmount = billAmountStr.toDoubleOrNull()
        if (billAmount == null || billAmount <= 0) {
            showError("Invalid amount")
            return
        }

        // Check balance
        val currentBalance = mainViewModel.balance.value ?: 0.0
        if (billAmount > currentBalance) {
            showError("Insufficient balance")
            return
        }

        // Process payment
        processBillPayment(billType, billAmount)
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun processBillPayment(billType: String, amount: Double) {
        // Validate bill payment
        if (validateBillPayment(amount)) {
            // Deduct amount from balance
            mainViewModel.updateBalance(-amount)

            // Create bill payment transaction
            val billTransaction = Transaction(
                id = UUID.randomUUID().toString(),
                amount = -amount,
                type = "bill",
                description = "$billType Payment",
                date = System.currentTimeMillis(),
                status = "Completed"
            )

            // Add transaction to recent transactions
            mainViewModel.addTransaction(billTransaction)

            // Show success message
            Snackbar.make(
                binding.root,
                "Bill payment successful",
                Snackbar.LENGTH_SHORT
            ).show()

            // Navigate back to dashboard
            findNavController().navigateUp()
        }
    }

    private fun validateBillPayment(amount: Double): Boolean {
        val currentBalance = mainViewModel.balance.value ?: 0.0

        if (amount <= 0) {
            showError("Invalid bill amount")
            return false
        }

        if (amount > currentBalance) {
            showError("Insufficient balance")
            return false
        }

        return true
    }
}