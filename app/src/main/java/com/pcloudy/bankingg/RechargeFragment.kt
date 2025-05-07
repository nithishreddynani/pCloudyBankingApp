package com.pcloudy.bankingg

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.pcloudy.bankingg.databinding.FragmentRechargeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RechargeFragment : Fragment() {
    private var _binding: FragmentRechargeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RechargeViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRechargeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupOperatorSpinner()
        setupCircleSpinner()
        setupRechargeTypeSpinner()
        setupRechargeButton()
        observeViewModel()
    }

    private fun setupRechargeButton() {
        binding.rechargeButton.setOnClickListener {
            validateAndPerformRecharge()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.rechargeState.collect { state ->
                when (state) {
                    is RechargeUiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.rechargeButton.isEnabled = false
                    }
                    is RechargeUiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.rechargeButton.isEnabled = true

                        // Create and add transaction
                        val rechargeTransaction = Transaction(
                            amount = -state.recharge.amount,
                            type = "recharge",
                            description = "${state.recharge.operator} Recharge - ${state.recharge.mobileNumber}"
                        )

                        // Add transaction to recent transactions
                        mainViewModel.addTransaction(rechargeTransaction)

                        // Show success message
                        Snackbar.make(
                            binding.root,
                            "Recharge Successful",
                            Snackbar.LENGTH_SHORT
                        ).show()

                        // Navigate back to Dashboard
                        navigateToDashboard()
                    }
                    is RechargeUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.rechargeButton.isEnabled = true

                        Snackbar.make(
                            binding.root,
                            state.message,
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    else -> {
                        binding.progressBar.visibility = View.GONE
                        binding.rechargeButton.isEnabled = true
                    }
                }
            }
        }
    }

    private fun navigateToDashboard() {
        try {
            // Try to navigate using SafeArgs if available
            findNavController().navigate(R.id.action_recharge_to_dashboard)
        } catch (e: Exception) {
            // Fallback navigation
            findNavController().navigateUp()
        }
    }

    private fun setupOperatorSpinner() {
        val operators = arrayOf(
            "Select Operator", "Airtel", "Jio", "VI", "BSNL", "Idea"
        )
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            operators
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.operatorSpinner.adapter = adapter
        binding.operatorSpinner.setSelection(0)
    }

    private fun setupCircleSpinner() {
        val circles = arrayOf(
            "Select Circle", "Maharashtra", "Gujarat", "Delhi",
            "Mumbai", "Karnataka", "Tamil Nadu"
        )
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            circles
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.circleSpinner.adapter = adapter
        binding.circleSpinner.setSelection(0)
    }

    private fun setupRechargeTypeSpinner() {
        val rechargeTypes = arrayOf("Select Type", "Prepaid", "Postpaid")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            rechargeTypes
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.rechargeTypeSpinner.adapter = adapter
        binding.rechargeTypeSpinner.setSelection(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun validateAndPerformRecharge() {
        // Null check for binding
        val currentBinding = _binding ?: return

        // Validation
        val mobileNumber = currentBinding.mobileNumberInput.text.toString().trim()
        val amountStr = currentBinding.amountInput.text.toString().trim()
        val operatorPosition = currentBinding.operatorSpinner.selectedItemPosition
        val circlePosition = currentBinding.circleSpinner.selectedItemPosition
        val rechargeTypePosition = currentBinding.rechargeTypeSpinner.selectedItemPosition

        // Comprehensive validation
        when {
            mobileNumber.length != 10 -> {
                currentBinding.mobileNumberInputLayout.error = "Invalid mobile number"
                return
            }
            amountStr.isEmpty() -> {
                currentBinding.amountInputLayout.error = "Enter recharge amount"
                return
            }
            operatorPosition == 0 -> {
                Snackbar.make(currentBinding.root, "Select Operator", Snackbar.LENGTH_SHORT).show()
                return
            }
            circlePosition == 0 -> {
                Snackbar.make(currentBinding.root, "Select Circle", Snackbar.LENGTH_SHORT).show()
                return
            }
            rechargeTypePosition == 0 -> {
                Snackbar.make(currentBinding.root, "Select Recharge Type", Snackbar.LENGTH_SHORT).show()
                return
            }
        }

        // Parse amount
        val amount = amountStr.toDoubleOrNull() ?: run {
            currentBinding.amountInputLayout.error = "Invalid amount"
            return
        }

        // Get selected values
        val operator = currentBinding.operatorSpinner.selectedItem.toString()
        val circle = currentBinding.circleSpinner.selectedItem.toString()
        val rechargeTypeString = currentBinding.rechargeTypeSpinner.selectedItem.toString()

        // Perform recharge with API simulation
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                mainViewModel.simulateApiCall("/mobile/recharge") {
                    // Ensure we're on the Main thread and binding is still valid
                    withContext(Dispatchers.Main) {
                        // Check binding again before using
                        val safeBinding = _binding
                        if (safeBinding != null) {
                            // Create recharge transaction
                            val rechargeTransaction = Transaction(
                                amount = -amount,
                                type = "recharge",
                                description = "$operator Recharge - $mobileNumber"
                            )

                            // Add transaction and update balance
                            mainViewModel.addTransaction(rechargeTransaction)

                            // Navigate to dashboard
                            findNavController().navigate(R.id.action_recharge_to_dashboard)
                        }
                    }
                }
            } catch (e: Exception) {
                // Check binding before showing Snackbar
                _binding?.let {
                    Snackbar.make(
                        it.root,
                        "Recharge failed: ${e.message}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}