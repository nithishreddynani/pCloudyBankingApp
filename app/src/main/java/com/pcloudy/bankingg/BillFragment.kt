package com.pcloudy.bankingg

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.pcloudy.bankingg.databinding.FragmentBillBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BillFragment : Fragment() {
    private var _binding: FragmentBillBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BillViewModel by viewModels()
    private lateinit var billsAdapter: BillsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBillBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupListeners()

        // Initial bill fetch
        viewModel.fetchBills()
    }

    private fun setupRecyclerView() {
        billsAdapter = BillsAdapter(
            onBillClick = { bill -> showBillDetails(bill) },
            onPayBillClick = { bill -> payBill(bill) }
        )
        binding.billsRecyclerView.apply {
            adapter = billsAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.bills.collect { bills ->
                billsAdapter.submitList(bills)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { errorMessage ->
                errorMessage?.let {
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.operationResult.collect { result ->
                result?.let {
                    when (it) {
                        BillViewModel.OperationResult.SUCCESS -> {
                            Snackbar.make(binding.root, "Operation successful", Snackbar.LENGTH_SHORT).show()
                        }
                        BillViewModel.OperationResult.FAILURE -> {
                            Snackbar.make(binding.root, "Operation failed", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.fabAddBill.setOnClickListener {
            showAddBillDialog()
        }
    }

    private fun showAddBillDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_bill, null)
        val billerNameEditText = dialogView.findViewById<TextInputEditText>(R.id.billerNameEditText)
        val billAmountEditText = dialogView.findViewById<TextInputEditText>(R.id.billAmountEditText)
        val dueDateEditText = dialogView.findViewById<TextInputEditText>(R.id.dueDateEditText)
        val billTypeSpinner = dialogView.findViewById<Spinner>(R.id.billTypeSpinner)

        // Setup date picker
        val calendar = Calendar.getInstance()
        dueDateEditText.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    dueDateEditText.setText(
                        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                            .format(calendar.time)
                    )
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Setup bill type spinner
        billTypeSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            BillType.values()
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add New Bill")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val bill = Bill(
                    billerName = billerNameEditText.text.toString(),
                    amount = billAmountEditText.text.toString().toDoubleOrNull() ?: 0.0,
                    dueDate = calendar.time,
                    billType = billTypeSpinner.selectedItem as BillType
                )
                viewModel.addBill(bill)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showBillDetails(bill: Bill) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Bill Details")
            .setMessage("""
                Biller: ${bill.billerName}
                Amount: ₹${String.format("%.2f", bill.amount)}
                Due Date: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(bill.dueDate)}
                Type: ${bill.billType}
                Status: ${if (bill.isPaid) "Paid" else "Pending"}
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }

    private fun payBill(bill: Bill) {
        viewModel.payBill(bill)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}