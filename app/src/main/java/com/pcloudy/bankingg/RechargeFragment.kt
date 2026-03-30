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
import com.pcloudy.bankingg.databinding.FragmentRechargeBinding
import kotlinx.coroutines.launch

class RechargeFragment : Fragment() {
    private var _binding: FragmentRechargeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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

    private fun setupOperatorSpinner() {
        val operators = arrayOf("Select Operator", "Airtel", "Jio", "VI", "BSNL", "Idea")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, operators)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.operatorSpinner.adapter = adapter
        binding.operatorSpinner.setSelection(0)
    }

    private fun setupCircleSpinner() {
        val circles = arrayOf("Select Circle", "Maharashtra", "Gujarat", "Delhi", "Mumbai", "Karnataka", "Tamil Nadu")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, circles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.circleSpinner.adapter = adapter
        binding.circleSpinner.setSelection(0)
    }

    private fun setupRechargeTypeSpinner() {
        val types = arrayOf("Select Type", "Prepaid", "Postpaid")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.rechargeTypeSpinner.adapter = adapter
        binding.rechargeTypeSpinner.setSelection(0)
    }

    private fun setupRechargeButton() {
        binding.rechargeButton.setOnClickListener { validateAndRecharge() }
    }

    private fun observeViewModel() {
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show() }
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading == true) View.VISIBLE else View.GONE
            binding.rechargeButton.isEnabled = loading != true
        }
    }

    private fun validateAndRecharge() {
        val mobile = binding.mobileNumberInput.text.toString().trim()
        val amountStr = binding.amountInput.text.toString().trim()
        val operatorPos = binding.operatorSpinner.selectedItemPosition
        val circlePos = binding.circleSpinner.selectedItemPosition
        val typePos = binding.rechargeTypeSpinner.selectedItemPosition

        when {
            mobile.length != 10 -> { binding.mobileNumberInputLayout.error = "Invalid mobile number"; return }
            amountStr.isEmpty() -> { binding.amountInputLayout.error = "Enter recharge amount"; return }
            operatorPos == 0 -> { showError("Select Operator"); return }
            circlePos == 0 -> { showError("Select Circle"); return }
            typePos == 0 -> { showError("Select Recharge Type"); return }
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) { binding.amountInputLayout.error = "Invalid amount"; return }

        val currentBalance = viewModel.balanceResponse.value?.balance ?: 0.0
        if (amount > currentBalance) { showError("Insufficient balance"); return }

        val operator = binding.operatorSpinner.selectedItem.toString()
        val circle = binding.circleSpinner.selectedItem.toString()
        val rechargeType = binding.rechargeTypeSpinner.selectedItem.toString()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.mobileRecharge(mobile, operator, circle, rechargeType, amount)
                Snackbar.make(binding.root, "Recharge successful!", Snackbar.LENGTH_SHORT).show()
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
